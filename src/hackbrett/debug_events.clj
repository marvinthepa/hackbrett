(ns hackbrett.debug-events)

; Other available midi events are:
; * :channel-pressure
; * :control-change
; * :note-off
; * :note-on
; * :pitch-bend
; * :poly-pressure
; * :program-change
;(on-event [:midi :note-on] (fn [{note :note velocity :velocity}]
;                             (println "Note: " note ", Velocity: " velocity))
;          ::note-printer)
;(doseq  [sym [:channel-pressure :control-change :node-off :pitch-bend :poly-pressure :program-change]]
;  (on-event [:midi sym]
;            (fn [event] (pprint event))
;            (symbol (str \: sym "-event"))))
