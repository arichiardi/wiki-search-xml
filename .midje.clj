(change-defaults :print-level :print-namespaces)

(when-not (running-in-repl?)
  (change-defaults :fact-filter #(and (not (:slow %))
                                      (not (:bench %)))))
