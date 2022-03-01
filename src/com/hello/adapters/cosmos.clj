(ns com.hello.adapters.cosmos
  (:require [com.hello.adapters.cosmos.jackson-workaround])
  (:import
   (com.azure.cosmos CosmosClientBuilder)
   (com.azure.cosmos.models PartitionKey)))

(defn create-client [{:keys [endpoint key]}]
  (-> (doto (new CosmosClientBuilder)
        (.endpoint endpoint)
        (.key key)
        (.gatewayMode))
      .buildClient))

(defn create-database [cosmos {:keys [name]}]
  (let [db-resp (.createDatabaseIfNotExists cosmos name)
        db-id (-> db-resp .getProperties .getId)]
    {:name name
     :database (.getDatabase cosmos db-id)}))

(defn create-container [cosmos-db {:keys [name partition-key] :or {partition-key "/id"}}]
  (let [cont-resp (.createContainerIfNotExists (:database cosmos-db) name partition-key)
        cont-id (-> cont-resp .getProperties .getId)]
    {:name name
     :container (.getContainer (:database cosmos-db) cont-id)}))


(defn create-item [container item]
  (bean (.createItem (:container container) item)))

(defn upsert-item [container item]
  (bean (.upsertItem (:container container) item)))

(defn read-item
  ([container id]
   (read-item container id id))
  ([container id partition-key]
   (bean (.readItem (:container container) id (PartitionKey. partition-key) ^Class Object))))

(defn get-item [container id]
  (let [item-resp (read-item container id)
        item (:item item-resp)]
    item))

(comment

  (def cosmos (create-client {:endpoint "https://localhost:8081"
                              :key "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw=="}))

  (def user-container
    (-> cosmos
        (create-database {:name "REITIT_HELLO"})
        (create-container {:name "user"
                           :partition-key "/id"
                           :partition-fn :id})))

  (create-item user-container {:name "Ala" :id "4598891"})

  (upsert-item user-container {:name "Ala" :id "4598891"
                               :address {:street "Błotna 666"
                                         :city "Parzymiechy"}})

  (:item (read-item user-container "4598891"))

  ;; => {:_self "dbs/n78+AA==/colls/n78+APgP0Ag=/docs/n78+APgP0AgBAAAAAAAAAA==/",
  ;;     :_attachments "attachments/",
  ;;     :_rid "n78+APgP0AgBAAAAAAAAAA==",
  ;;     :name "Ala",
  ;;     :_ts 1646127954,
  ;;     :_etag "\"00000000-0000-0000-2d51-24cdc53e01d8\"",
  ;;     :id "4598891"}

  ,)
