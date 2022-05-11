(ns crypto)

(defn hex-str
  "Return the hexadecimal string representation of a byte array"
  [ba]
  (apply str (map #(format "%02x" %) ba)))

(defn hash256 [s]
  (-> (java.security.MessageDigest/getInstance "sha-256")
      (.digest (.getBytes s))
      hex-str))

(defn pwd-hash [name password]
  (hash256 (str name password)))
