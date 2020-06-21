&nbsp;

<img width=250 src="s+logo.jpg"/>

&nbsp;

# Locate your styles.

**Style-plus** enables co-location of styles within ClojureScript Reagent components.

The library provides a thin syntactical abstraction over the excellent [Stylefy](https://github.com/Jarzka/stylefy) library, as well as some additional css helper functions.

The motivation behind **style-plus** is to completely eliminate the need to write and maintain separate style sheets for projects targeting browser environments.

&nbsp;

## Usage
First, add the dependency to your project:
```Clojure
[paintparty/style-plus "0.5.0-SNAPSHOT"]
```
&nbsp;

Then, follow the [Setup Instructions](https://github.com/Jarzka/stylefy#setup) for **Stylefy**.

&nbsp;

Lastly, require **style-plus** into the namespace(s) as needed:
```Clojure
(:require [style-plus.core :as style-plus :refer [s+]])
```

&nbsp;

Now, you can style your components w the following features:
1) Media queries and pseudo-classes at a property-level
2) The same implicit integer-to-pixel convention that Reagent uses
3) Garden syntax (Stylefy uses garden under the hood), such as `[[10 20]]`
4) Optionally attach a ns-qualifed custom data attribute.

&nbsp;

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

&nbsp;

## Helpers

**style-plus** ships with 3 helper functions for defining css breakpoints.

In the example below, a number of global breakpoints are defined in a dedicated namespace.
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
  (:require [my-project.breakpoints :refer [sm lg]]))

(defn header [text]
  [:h1
    (s+ {:font-size {:= 18 sm 16 lg 24}})
    text])
```

&nbsp;

&nbsp;

## Using Metadata

Co-locating your style inside components obviates the need to use class names and css selectors. The html generated in the DOM will have many auto-generated class names (like the ones above), and possibly some utility classes, if you are using an atomic css library. As a result, it can become difficult to quickly comprehend the source location when looking at elements in a browser inspector (such as Chrome DevTool Elements panel).

With `s+`, you can add metadata to the style-map (first arg), which will then be transformed into a unique value and attached to the element as a custom data attribute called `data-ns`. This metadata should be a map with single entry. The key is the var-quoted function name and the value is a user-defined keyword (which should have some kind of symantic relationship to the actual element).

```Clojure
(defn my-button [label]
  (let [f #'my-button]
    [:div
     (s+ ^{#'f :outer}
         {:cursor :pointer
          :text-align :center
          :border [[1 :solid :blue]]}
         {:role :button
          :on-click #()})
     [:span
      (s+ ^{#'f :inner}
          {:background :yellow})
      label]])
```
In the resulting html the namespace, function, element-name, and source line number are clearly evident:
```Html
<div class="_stylefy_-545329968"
     data-ns="my-project.ui/my-button::outer:619"
     role="button" >
  <div data-ns="my-project.ui/my-button::inner:619"
       class="_stylefy_457554977">Go</div>
</div>
```


If want to attach the identifying custom-data attribute manually, or use a different key than the default `:data-ns`(`:data-foo` is used in the example below), you can make use of `style-plus/ns+`:
```Clojure
;; Require it into your namespace
(ns my-project.ui
  (:require [style-plus.core :refer [s+ ns+]]))


;; Use ns+ in the html attributes map
(defn my-button [label]
  [:div
  (s+ {:cursor :pointer
       :text-align :center
       :border [[1 :solid :blue]}
      {:role :button
       :on-click #()
       :data-foo (ns+ #'my-button :outer)})
    [:span
      (s+ {:background :yellow}
          {:data-foo (ns+ #'my-button :inner})
      label]])
```
The second keyword argument to `ns+` is optional. You can also call it like this:
```Clojure
(ns+ #'my-button)
;; => "my-project.ui/my-button:619"
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
