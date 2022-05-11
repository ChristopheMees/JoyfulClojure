(ns hello.html
  (:require [hello.state :as s]
            [reagent.core :refer [as-element]]))

#_(defn map->attr [m]
  (let [[k v] (first m)]
    (str (name k) "=\"" v "\"")))

#_(defn el->html [element]
  (let [key (first element)
        attr (when (map? (second element)) (second element))
        content (last element)
        otag (if attr
               (str \< (name key) " " (map->attr attr) \>)
               (str \< (name key) \>))
        ctag (str "</" (name key) \>)
        stag (str \< (name key) "/>")]
    (if content
      (str otag content ctag)
      stag)))

#_(defn ->html [dom]
  (let [key (first dom)
        content (last dom)]
    (if (coll? content)
      (if (coll? (first content))
        (el->html [key (reduce (fn [html el] (str html (->html el))) "" content)])
        (el->html [(first content) (second content) (last content)]))
      (el->html dom))))

(defn ->td [idx row]
  (fn [i header]
    (let [v (row header)]
      [:td {:key (str idx i)} (if (inst? v) (.toLocaleDateString v) (str v))])))

(comment ((->td 0 {:a 1}) 0 :a)
         ((->td 0 {:a #inst "2022-01-04"}) 0 :a))

(defn ->tr [headers]
  (fn [idx row] [:tr {:key idx} (map-indexed (->td idx row) headers)]))

(comment ((->tr [:a :b :c]) 0 {:a 1 :b 2 :c 3}))

(defn ->trows [headers rows]
  (map-indexed (->tr headers) rows))

(comment (->trows [:a :b :c] [{:a 1 :b 2 :c 3}
                              {:a 4 :b 5 :c 6}]))

(defn ->theaders [headers]
  (map (fn [h] [:th {:key (str h)} h]) headers))

(defn ->table [{:keys [headers rows]}]
  (let [theader [:thead (->theaders headers)]
        trows [:tbody (->trows headers rows)]]
    [:table.table theader trows]))

(comment
  (->> [:a :b :c] (map (fn [h] [:th h])) (cons :tr) (into []))
  
  (->table {:headers [:a :b :c]
            :rows [{:a 1 :b 2 :c 3}
                   {:a 4 :b 5 :c 6}]}))

(defn input-label [form-id {:keys [key label]}]
  [:label.form-label {:key (str form-id key :label)} label])

(defn input-change [form-id key]
  #(swap! s/state assoc-in [form-id key] (.-target.value %)))

(defn input-control [state form-id {:keys [key type]}]
  [:input.form-control {:key (str form-id key :input)
                        :type type
                        :on-change (input-change form-id key)
                        :value (get-in state [form-id key])}])

(defn input-select [state form-id {:keys [key options default-option]}]
  (let [val (get-in state [form-id key])]
    [:select.form-select
     {:on-change (input-change form-id key)}
     (when-not val [:option {:key (str form-id key "default")
                             :selected true} default-option])
     (map (fn [{:keys [label value]}] [:option 
                                       {:key (str form-id key value)
                                        :selected (= val value)
                                        :value value} label])
          options)]))

(comment
  (as-element
   (input-select {:my-form {:freq "2"}}
                 :my-form
                 {:key :freq
                  :default-option "default"
                  :options [{:label "Option1" :value :opt1}
                            {:label "Option2" :value :opt2}]})))

(defn input [state form-id {:keys [key type] :as opts}]
  [:div.mb-3 {:key (str form-id key :div)}
   [input-label form-id opts]
   (if-not (= type "select")
     [input-control state form-id opts]
     [input-select state form-id opts])])

(defn submit-btn [{:keys [content on-click]}]
  [:button.btn.btn-primary {:type "submit" :on-click on-click} content])

(defn ->form [state {:keys [btn id inputs]}]
  [:form
   {:id id}
   (map (partial input state id) inputs)
   [submit-btn btn]])

(defn toggle-form [id]
  (swap! s/state update-in [id :active] not))

(defn close-form [id]
  (swap! s/state dissoc id))

(defn new-form-btn [id text]
  [:button.btn.btn-primary {:on-click #(toggle-form id)} text])

(defn ->smart-form [state {id :id :as opts}]
  (if (get-in state [id :active])
    [->form state opts]
    [new-form-btn id (get-in opts [:toggle :btn-text])]))
