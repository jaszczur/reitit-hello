(ns com.hello.adapters.cosmos.jackson-workaround
  (:require [jsonista.core :as j])
  (:import
   (jsonista.jackson
    DateSerializer
    FunctionalKeyDeserializer
    FunctionalSerializer
    KeywordSerializer
    KeywordKeyDeserializer
    HashMapDeserializer
    ArrayListDeserializer
    PersistentHashMapDeserializer
    PersistentVectorDeserializer
    SymbolSerializer
    RatioSerializer FunctionalKeywordSerializer)
   (com.fasterxml.jackson.core JsonGenerator$Feature JsonFactory)
   (com.fasterxml.jackson.databind
    JsonSerializer ObjectMapper module.SimpleModule
    SerializationFeature DeserializationFeature Module)
   (com.fasterxml.jackson.databind.module SimpleModule)
   (java.io InputStream Writer File OutputStream DataOutput Reader)
   (java.net URL)
   (com.fasterxml.jackson.datatype.jsr310 JavaTimeModule)
   (java.util List Map Date)
   (clojure.lang Keyword Ratio Symbol)
   (com.azure.cosmos.implementation Utils)))

;; Stealed from Jsonista - need to ask for making this function public so others can attach clojure module to
;; existing ObjectMapper
(defn- clojure-module
  "Create a Jackson Databind module to support Clojure datastructures."
  [{:keys [encode-key-fn decode-key-fn encoders date-format]
    :or {encode-key-fn true, decode-key-fn false}}]
  (doto (SimpleModule. "Clojure")
    (.addDeserializer List (PersistentVectorDeserializer.))
    (.addDeserializer Map (PersistentHashMapDeserializer.))
    (.addSerializer Keyword (KeywordSerializer. false))
    (.addSerializer Ratio (RatioSerializer.))
    (.addSerializer Symbol (SymbolSerializer.))
    (.addSerializer Date (if date-format
                           (DateSerializer. date-format)
                           (DateSerializer.)))
    (as-> module
        (doseq [[type encoder] encoders]
          (cond
            (instance? JsonSerializer encoder) (.addSerializer module type encoder)
            (fn? encoder) (.addSerializer module type (FunctionalSerializer. encoder))
            :else (throw (ex-info
                          (str "Can't register encoder " encoder " for type " type)
                          {:type type, :encoder encoder})))))
    (cond->
        (true? decode-key-fn) (.addKeyDeserializer Object (KeywordKeyDeserializer.))
        (fn? decode-key-fn) (.addKeyDeserializer Object (FunctionalKeyDeserializer. decode-key-fn))
        (true? encode-key-fn) (.addKeySerializer Keyword (KeywordSerializer. true))
        (fn? encode-key-fn) (.addKeySerializer Keyword (FunctionalKeywordSerializer. encode-key-fn)))))

;; This one uses internal function from azure-cosmos SDK. Would be great if it's public
(doto (Utils/getSimpleObjectMapper)
  (.registerModule (clojure-module {:encode-key-fn true, :decode-key-fn true})))
