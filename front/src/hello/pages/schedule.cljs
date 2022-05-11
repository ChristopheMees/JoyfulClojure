(ns hello.pages.schedule
  (:require [hello.html :refer [->table]]))

(defn schedule-page [state]
  [:div
   [:h1 "People in Places"]
   [->table {:headers [:person/first-name
                       :person/last-name
                       :place/description
                       :task/description
                       :schedule/start-time
                       :schedule/end-time]
             :rows (state :schedule)}]])
