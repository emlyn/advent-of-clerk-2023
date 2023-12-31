;; # 🎄 Advent of Clerk: Day 10
(ns advent-of-clerk.day-10
  (:require [nextjournal.clerk]
            [emlyn.grid :as g]
            #_[clojure.string :as str]))

(def example "7-F7-
.FJ|7
SJLL7
|F--J
LJ.LJ")

(defn dirs
  "Get directions a cell can connect to"
  [c]
  (case c
    \S [[-1 0] [0 -1] [0 1] [1 0]]
    \| [[0 -1] [0 1]]
    \- [[-1 0] [1 0]]
    \L [[0 -1] [1 0]]
    \J [[0 -1] [-1 0]]
    \7 [[-1 0] [0 1]]
    \F [[0 1] [1 0]]
    \. []
    nil []
    (throw (Exception. (str "Unrecognised cell: " c)))))

(defn neighbours
  [grid [x y]]
  (for [[dx dy] (dirs (grid [x y]))
        [nx ny] (dirs (grid [(+ x dx) (+ y dy)]))
        :when (and (= dx (- nx)) (= dy (- ny)))]
    [(+ x dx) (+ y dy)]))

(defn find-cell
  [grid c]
  (->> grid
       (g/map-kv (fn [[x y] v]
                   (when (= v c) [x y])))
       vals
       (remove nil?)
       first))

(find-cell (g/grid example) \S)

(defn part1
  [s]
  (let [grid (g/grid s)]
    (loop [seen (g/map-vals #(= % \S) grid)
           steps 0
           pos [(find-cell grid \S)]]
      (if-let [next (seq (for [p pos
                               n (neighbours grid p)
                               :when (not (seen n))]
                           n))]
        (recur (reduce (fn [s p]
                         (assoc s p true))
                       seen
                       next)
               (inc steps)
               next)
        steps))))

(part1 example)

#_(part1 (slurp "input_10.txt"))

(defn get-loop
  "Which cells are part of the loop?
   Basically `part1` but return the `seen` grid instead of the count"
  [s]
  (let [grid (g/grid s)]
    (loop [seen (g/map-vals #(= % \S) grid)
           steps 0
           pos [(find-cell grid \S)]]
      (if-let [next (seq (for [p pos
                               n (neighbours grid p)
                               :when (not (seen n))]
                           n))]
        (recur (reduce (fn [s p]
                         (assoc s p true))
                       seen
                       next)
               (inc steps)
               next)
        seen))))

(g/map-vals #(if % \# \.) (get-loop example))

(defn num-crossings
  [cells]
  (->> cells
       (apply str)
       (re-seq #"F-*J|L-*7|\|")
       count))

(num-crossings ".|.L---7||F7F--7|") ;; expect 5

(defn inside?
  [grid inloop [x y]]
  (and (not (inloop [x y]))
       (let [lcells (map #(if (inloop [% y])
                           (grid [% y])
                           \.)
                        (range x))
             rcells (map #(if (inloop [% y])
                            (grid [% y])
                            \.)
                         (range (inc x) (inc (count (first grid)))))
             cells (if (some #{\S} lcells)
                     rcells
                     lcells)]
         (->> cells
              num-crossings
              odd?))))

(defn part2
  [s]
  (let [grid (g/grid s)
        seen (get-loop s)]
    (->> (g/map-kv (fn [[x y] _]
                     (inside? grid seen [x y]))
                   grid)
         (mapcat identity)
         (filter true?)
         count)))

(def example2 "...........
.S-------7.
.|F-----7|.
.||.....||.
.||.....||.
.|L-7.F-J|.
.|..|.|..|.
.L--J.L--J.
...........")

(part2 example2) ;; expect 4

(def example3 ".F----7F7F7F7F-7....
.|F--7||||||||FJ....
.||.FJ||||||||L7....
FJL7L7LJLJ||LJ.L-7..
L--J.L7...LJS7F-7L7.
....F-J..F7FJ|L7L7L7
....L7.F7||L7|.L7L7|
.....|FJLJ|FJ|F7|.LJ
....FJL-7.||.||||...
....L---J.LJ.LJLJ...")

(part2 example3) ;; expect 8

(def example4 "FF7FSF7F7F7F7F7F---7
L|LJ||||||||||||F--J
FL-7LJLJ||||||LJL-77
F--JF--7||LJLJ7F7FJ-
L---JF-JLJ.||-FJLJJ7
|F|F-JF---7F7-L7L|7|
|FFJF7L7F-JF7|JL---7
7-L-JL7||F7|L7F-7F7|
L.L7LFJ|||||FJL7||LJ
L7JLJL-JLJLJL--JLJ.L")

(part2 example4) ;; expect 10

#_(part2 (slurp "input_10.txt"))

#_(let [grid (g/grid (slurp "input_10.txt"))
      path (get-loop (slurp "input_10.txt"))
      cont (g/map-kv (fn [[x y] _]
                       (inside? grid path [x y]))
                     grid)]
  (->> (g/map-kv (fn [[x y] c]
                   (cond (cont [x y]) \#
                         (path [x y]) c
                         :else \.))
                 grid)
       g/as-rows
       (map (partial apply str))
       (str/join \newline)
       (spit "debug.txt")))
