(ns hello.state
  (:require [reagent.core :as r]))

(defonce state (r/atom {:active-page :customers}))
