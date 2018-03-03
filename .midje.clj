(change-defaults :print-level :print-namespaces
                 :fact-filter #(and (not (:slow %1))
                                    (not (:bench %1))
                                    (not (:profile %1))))
