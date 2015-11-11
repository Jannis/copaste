(ns server.parsing.copaste
  (:require [clj-consonant.store :as s]
            [server.parser :refer [mutatef readf]]
            [server.transaction :as t]))

;;;; Prepare Consonant objects for Om

(defn property->om [[k v]]
  [(keyword (str "property/" (name k))) v])

(defn properties->om [properties]
  (into {} (map property->om) properties))

(defn object->om [object]
  (reduce (fn [object [k v]] (assoc object k v))
          (dissoc object :properties)
          (properties->om (:properties object))))

;;;; Prepare Om objects for Consonant

(defn om-property? [[k v]]
  (re-matches (re-pattern #"^:property.*") (str k)))

(defn om->property [[k v]]
  [k (keyword (name k)) v])

(defn om->properties [object]
  (->> object
       (filter om-property?)
       (map om->property)))

(defn om->object [object]
  (reduce (fn [object [ok k v]] (-> object
                                    (assoc-in [:properties k] v)
                                    (dissoc ok)))
          object
          (om->properties object)))

;;;; Reads

(defmethod readf :copaste/refs
  [{:keys [consonant selector]} _ _]
  {:value (->> (s/get-refs consonant)
               vals
               (map #(select-keys % selector))
               (into []))})

(defmethod readf :copaste/snippets
  [{:keys [consonant selector]} _ {:keys [ref]}]
  {:value (->> (s/get-objects consonant (second ref) "snippet")
               (map object->om)
               (map #(select-keys % selector))
               (into []))})

;;;; Mutations

(defn update-snippet [consonant ref snippet]
  (t/transact! consonant
    (-> (t/begin :source (some-> ref :head :sha1))
        (t/update :uuid (:uuid snippet) :properties (:properties snippet))
        (t/commit :target (if ref (:name ref) "HEAD")
                  :author {:name "Jannis Pohlmann" :email "jannis@xfce.org"}
                  :committer {:name "copaste" :email "copaste@copaste.org"}
                  :message (str "Update snippet " (:uuid snippet))))))

(defn create-snippet [consonant ref snippet]
  (t/transact! consonant
    (-> (t/begin :source (some-> ref :head :sha1))
        (t/create :class "snippet" :properties (:properties snippet))
        (t/commit :target (if ref (:name ref) "HEAD")
                  :author {:name "Jannis Pohlmann" :email "jannis@xfce.org"}
                  :committer {:name "copaste" :email "copaste@copaste.org"}
                  :message "Create snippet"))))

(defmethod mutatef 'copaste/save-snippet
  [{:keys [consonant]} _ {:keys [ref snippet]}]
  {:value {:keys [:copaste/refs :copaste/snippets]
           :tempids {}}
   :action (fn []
             (let [ref (if ref ref (s/get-ref consonant "HEAD"))
                   snippet (om->object snippet)]
               (if (:uuid snippet)
                 (update-snippet consonant ref snippet)
                 (create-snippet consonant ref snippet))))})
