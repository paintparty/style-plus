![logo](s+logo.jpg)


# style-plus

**style-plus** enables co-location of styles within ClojureScript Reagent components. The library provides a thin syntactical abstraction over the excellent [Stylefy](https://github.com/Jarzka/stylefy) library, as well as some additional css helper functions. The motivation is to completely eliminate the need to write and maintain separate style sheets.

## Usage
Add the dependency to your project:
```Clojure
[paintparty/style-plus "0.3.2"]
```
&nbsp;

Follow the [Setup Instructions](https://github.com/Jarzka/stylefy#setup) for **Stylefy**.

&nbsp;

Require style-plus into the namespace(s) as needed:
```Clojure
(:require [style-plus.core :as style-plus :refer [s+]])
```

&nbsp;

Now you can style your components w the following features:
1) The same implicit integer-to-pixel convention that Reagent uses
2) Garden syntax (Stylefy uses garden under the hood), such as `[[10 20]]`
3) Media queries and pseudo-classes at a property-level

The `s+` function takes a map of styles, and an optional map of html attributes. Media-queries are expressed as maps, while psuedo-classes are expressed as strings:
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

With the above example, Stylefy would automatically produce and inject the following selectors into your document, as well attaching the appropriate class names to the element in your component:
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
