<img width=300 src="s+logo.jpg"/>

&nbsp;

**style-plus** enables co-location of styles within ClojureScript Reagent components. The library provides a thin syntactical abstraction over the excellent [Stylefy](https://github.com/Jarzka/stylefy) library, as well as some additional css helper functions. The motivation is to completely eliminate the need to write and maintain separate style sheets.

## Usage
Add the dependency to your project:
```Clojure
[paintparty/style-plus "0.3.2"]
```
&nbsp;

Follow the [Setup Instructions](https://github.com/Jarzka/stylefy#setup) for **Stylefy**.

&nbsp;

Require **style-plus** into the namespace(s) as needed:
```Clojure
(:require [style-plus.core :as style-plus :refer [s+]])
```

&nbsp;

Now you can style your components w the following features:
1) The same implicit integer-to-pixel convention that Reagent uses
2) Garden syntax (Stylefy uses garden under the hood), such as `[[10 20]]`
3) Media queries and pseudo-classes at a property-level

The `s+` function takes a map of styles, and an optional map of html attributes. Media-queries are expressed as maps, while psuedo-classes are expressed as strings. A key of `:=` represents the default value.

```Clojure
(defn button [label]
  [:div
    (s+ {:cursor :pointer
         :text-align :center
         :padding [[10 20]]
         :color {:= :blue
                 "hover" :red}
         :font-size {:= 18
                     {:max-width 500} 16
                     {:max-width 700} 20}}
         :border {:= [[1 :solid :blue]]
                  "hover" [[1 :solid :red]]}
        {:role :button
         :on-click #()})
    label])
```

&nbsp;

With the above example, **Stylefy** automatically injects the following selectors into your document, as well attaching the appropriate class names to the element in your component:
```css
._stylefy_1838088000 {
    border: 1px solid blue;
    padding: 10px 20px;
    text-align: center;
    cursor: pointer;
    font-size: 18px;
    color: blue;
}
._stylefy_1838088000:hover {
    color: red;
    border: 1px solid red;
}
```
&nbsp;

## Helpers

**style-plus** ships with a set of helpers for defining css breakpoints.

In the example below, some breakpoints are defined in a dedicated namespace.
```Clojure
(ns my-project.breakpoints
  (:require [style-plus.core :refer [above below between]]))

;; Define some breakpoints
(def bp
 {:sm 576
  :md 768
  :lg 992
  :xl 1200})

;; If you are designing for laptop/desktop
;; (below :sm bp) => {:max-width 575.98px}
(def sm (below :sm bp))
(def md (below :md bp))
(def lg (below :lg bp))
(def xl (below :xl bp))
(def md-only (between :sm :md bp))
(def md-xl (between :md :xl bp))

;; If you are designing for mobile-first
;; (below :sm bp) => {:min-width 576px}
; (def sm (above :sm bp))
; (def md (above :md bp))
; (def lg (above :lg bp))
; (def xl (above :xl bp))
; (def md-only (between :sm :md bp))
; (def md-xl (between :md :xl bp))
```

&nbsp;

These breakpoint defs can then be used with **style-plus**.
```Clojure
(ns my-project.ui
  (:require [my-project.breakpoints :refer [sm md lg xl md-only md-xl]]))

(defn header [text]
  [:h1
    (s+ {:font-size {:= 18 sm 16 xl 24}})
    text])
```




## License

Copyright © 2020 JC

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
