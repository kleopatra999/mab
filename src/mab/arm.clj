(ns mab.arm)

(defn create-arm
  "Create a bandit arm."
  ([count value] {:count count
                  :value value})
  ([]            (create-arm 0 0)))


(defn initialize-arm-map
  "Create a vector af initialized arms."
  ([n]
   (zipmap (range n) (take n (repeatedly create-arm))))
  ([counts values]
   (zipmap (range (count counts)) (map create-arm counts values))))

(defn arm-count
  "Get the count from an arm."
  [arm]
  (get arm :count 0))

(defn update-count
  "Update the count of an arm."
  [arm count]
  (assoc arm :count count))

(defn inc-count
  "Increment the count of an arm."
  [arm]
  (update-in arm [:count] inc))

(defn arm-value
  "Get the value from an arm."
  [arm]
  (get arm :value 0))

(defn update-value
  "Update the value from an arm."
  [arm value]
  (assoc arm :value value))

(defn arm-score 
  "Get the score from an arm. Scores are ephemeral values associated with an arm."
  [arm]
  (get arm :score 0))

(defn update-score 
  "Update the value from an arm. Scores are ephemeral values associated with an arm."
  [arm score]
  (assoc arm :score score))

(defn compute-update-value
  "Compute the updated value for an arm."
  [current-count current-value reward]
  (+
    (float (* (/ (- current-count 1) current-count) current-value))
    (float (* (/ 1 current-count) reward))))


(defn arm-by-idx
  "Get an arm by the index."
  [arms idx]
  (arms idx))

(defn replace-arm
  "Replace the arm at idx in arms with the given arm."
  [arms idx arm]
  (assoc arms idx arm))

(defn update-arm
  "Update an arm with a reward."
  [arms idx reward]
  (let [chosen-arm (arms idx (create-arm 0 0))
        chosen-arm (inc-count chosen-arm)
        current-count (arm-count chosen-arm)
        current-value (arm-value chosen-arm)
        new-value (compute-update-value current-count current-value reward)]
    (assoc arms idx (update-value chosen-arm new-value))))

(defn tuple-idx
  "Given an arm tuple (idx, arm) returns the index."
  [t]
  (first t))

(defn tuple-arm
  "Given an arm tuple (idx, arm) returns the arm."
  [t]
  (second t))

(defn max-value-tuple
  "Returns a arm tuple for the arm with the maximum value."
  [arms]
  (apply max-key (comp arm-value tuple-arm) arms))

(defn max-score-tuple
  "Returns a arm tuple for the arm with the maximum score."
  [arms]
  (apply max-key (comp arm-score tuple-arm) arms))

(defn total-arm-counts
  "Sum the total counts for the arms."
  [arms]
  (reduce + 0 (map arm-count (vals arms))))


(defn random-arm-idx
  "Get a random arm index."
  [arms]
  (nth (keys arms) (rand-int (count arms))))


(defn random-arm-tuple
  "Get a random arm."
  [arms]
  (nth (seq arms) (rand-int (count arms))))


(defn remove-arm
  "Remove the arm with \"index\" from \"arms\"."
  [arms idx]
  (dissoc arms idx))


(defn select-n-arms
  "Selects n distinct arms. Good for selecting n distinct arms using a given selection funcition. 
  Returns the n keys of the selected arms."
  [selectfn arms n]
  (loop [a arms
         ret []]
    (cond (or (= (count ret) n) (empty? a)) ret
          :else
          (let [selected (selectfn a)]
            ; this would be faster using dissoc and a map
            (recur (remove-arm a (first selected)) (conj ret (first selected)))))))


(defn map-on-arm-vals
  "map f over the values of m.
    Example (map-on-arm-vals inc-count arms)"
  [f m]
  (into {} (for [[k v] m] [k (f v)])))
