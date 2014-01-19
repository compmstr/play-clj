(ns play-clj.ui
  (:require [play-clj.g2d :as g2d]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Files Gdx]
           [com.badlogic.gdx.graphics Color Texture]
           [com.badlogic.gdx.graphics.g2d TextureRegion]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.ui ButtonGroup CheckBox Dialog
            HorizontalGroup Image ImageButton ImageTextButton Label ScrollPane
            SelectBox Skin Slider Stack Table TextButton TextField Tree
            Tree$Node VerticalGroup WidgetGroup Window]
           [com.badlogic.gdx.scenes.scene2d.utils NinePatchDrawable
            SpriteDrawable TextureRegionDrawable TiledDrawable]
           [com.esotericsoftware.tablelayout Cell]))

(defmacro drawable
  [type & options]
  `(~(symbol (str u/main-package ".scenes.scene2d.ui."
                  (u/key->pascal type) "Drawable."))
     ~@options))

(defmacro style
  [type & options]
  `(~(symbol (str u/main-package ".scenes.scene2d.ui."
                  (u/key->pascal type) "$"
                  (u/key->pascal type) "Style."))
     ~@options))

(defmacro skin
  [path & options]
  `(u/calls! ^Skin (Skin. (.internal ^Files (Gdx/files) ~path)) ~@options))

(defmacro align
  [key]
  `(u/static-field-lower :scenes :scene2d :utils :Align ~key))

(defn cell!
  [^Cell cell & args]
  (let [method (first args)
        [[a1 a2 a3 a4] rest-args] (split-with #(not (keyword? %)) (rest args))]
    (when method
      (case method
        :width (.width cell ^double a1)
        :height (.height cell ^double a1)
        :size (.size cell ^double a1 ^double a2)
        :min-width (.minWidth cell ^double a1)
        :min-height (.minHeight cell ^double a1)
        :min-size (.minSize cell ^double a1 ^double a2)
        :max-width (.maxWidth cell ^double a1)
        :max-height (.maxHeight cell ^double a1)
        :max-size (.minSize cell ^double a1 ^double a2)
        :space (.space cell ^double a1 ^double a2 ^double a3 ^double a4)
        :space-top (.spaceTop cell ^double a1)
        :space-left (.spaceLeft cell ^double a1)
        :space-bottom (.spaceBottom cell ^double a1)
        :space-right (.spaceRight cell ^double a1)
        :pad (.pad cell ^double a1 ^double a2 ^double a3 ^double a4)
        :pad-top (.padTop cell ^double a1)
        :pad-left (.padLeft cell ^double a1)
        :pad-bottom (.padBottom cell ^double a1)
        :pad-right (.padRight cell ^double a1)
        :fill (.fill cell ^boolean a1 ^boolean a2)
        :fill-x (.fillX cell)
        :fill-y (.fillY cell)
        :align (.align cell (int a1))
        :center (.center cell)
        :top (.top cell)
        :left (.left cell)
        :bottom (.bottom cell)
        :right (.right cell)
        :expand (.expand cell ^boolean a1 ^boolean a2)
        :expand-x (.expandX cell)
        :expand-y (.expandY cell)
        :ignore (.ignore cell ^boolean a1)
        :colspan (.colspan cell (int a1))
        :uniform (.uniform cell ^boolean a1 ^boolean a2)
        :uniform-x (.uniformX cell)
        :uniform-y (.uniformY cell)
        :row (.row cell)
        (u/throw-key-not-found method))
      (apply cell! cell rest-args))
    cell))

(defmulti add-to-group! #(-> % first :object type) :default WidgetGroup)

(defmethod add-to-group! WidgetGroup
  [[parent child]]
  (.addActor ^WidgetGroup (:object parent) ^Actor (:object child)))

(defmethod add-to-group! Table
  [[parent child]]
  (cond
    (map? child)
    (.add ^Table (:object parent) ^Actor (:object child))
    (coll? child)
    (apply cell! (add-to-group! [parent (first child)]) (rest child))
    (keyword? child)
    (case child
      :row (.row ^Table (:object parent))
      (u/throw-key-not-found child))))

(defn ^:private create-tree-node
  [child]
  {:object (Tree$Node. ^Actor (:object child))})

(defn ^:private add-tree-nodes
  [parent children]
  (when-let [node (add-to-group! [parent (first children)])]
    (doseq [child (rest children)]
      (add-to-group! [node child]))
    node))

(defmethod add-to-group! Tree
  [[parent child]]
  (cond
    (map? child)
    (let [node (create-tree-node child)]
      (.add ^Tree (:object parent) ^Tree$Node (:object node))
      node)
    (coll? child)
    (add-tree-nodes parent child)))

(defmethod add-to-group! Tree$Node
  [[parent child]]
  (cond
    (map? child)
    (let [node (create-tree-node child)]
      (.add ^Tree$Node (:object parent) ^Tree$Node (:object node))
      node)
    (coll? child)
    (add-tree-nodes parent child)))

(defn add!
  [group & children]
  (doseq [child children]
    (add-to-group! [group child]))
  group)

(defn ^:private create-group
  [^WidgetGroup group children]
  (apply add! (u/create-entity group) children))

; widgets

(defn check-box*
  [^String text arg]
  (u/create-entity (CheckBox. text arg)))

(defmacro check-box
  [text arg & options]
  `(let [entity# (check-box* ~text ~arg)]
     (u/calls! ^CheckBox (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro check-box!
  [entity k & options]
  `(u/call! ^Checkbox (u/get-obj ~entity :object) ~k ~@options))

(defn dialog*
  [text arg]
  (u/create-entity (Dialog. text arg)))

(defmacro dialog
  [text arg & options]
  `(let [entity# (dialog* ~text ~arg)]
     (u/calls! ^Dialog (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro dialog!
  [entity k & options]
  `(u/call! ^Dialog (u/get-obj ~entity :object) ~k ~@options))

(defn horizontal*
  [children]
  (create-group (HorizontalGroup.) children))

(defmacro horizontal
  [children & options]
  `(let [entity# (horizontal* ~children)]
     (u/calls! ^HorizontalGroup (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro horizontal!
  [entity k & options]
  `(u/call! ^HorizontalGroup (u/get-obj ~entity :object) ~k ~@options))

(defn image*
  [arg]
  (u/create-entity
    (cond
      (map? arg)
      (Image. ^TextureRegion (:object arg))
      (string? arg)
      (Image. (Texture. ^String arg))
      :else
      (Image. arg))))

(defmacro image
  [arg & options]
  `(let [entity# (image* ~arg)]
     (u/calls! ^Image (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image!
  [entity k & options]
  `(u/call! ^Image (u/get-obj ~entity :object) ~k ~@options))

(defn image-button*
  [arg]
  (u/create-entity (ImageButton. arg)))

(defmacro image-button
  [arg & options]
  `(let [entity# (image-button* ~arg)]
     (u/calls! ^ImageButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image-button!
  [entity k & options]
  `(u/call! ^ImageButton (u/get-obj ~entity :object) ~k ~@options))

(defn image-text-button*
  [^String text arg]
  (u/create-entity (ImageTextButton. text arg)))

(defmacro image-text-button
  [text arg & options]
  `(let [entity# (image-text-button* ~text ~arg)]
     (u/calls! ^ImageTextButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image-text-button!
  [entity k & options]
  `(u/call! ^ImageTextButton (u/get-obj ~entity :object) ~k ~@options))

(defn label*
  [^String text arg]
  (u/create-entity
    (if (isa? (type arg) Color)
      (Label. text (style :label (g2d/bitmap-font) arg))
      (Label. text arg))))

(defmacro label
  [text arg & options]
  `(let [entity# (label* ~text ~arg)]
     (u/calls! ^Label (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro label!
  [entity k & options]
  `(u/call! ^Label (u/get-obj ~entity :object) ~k ~@options))

(defn scroll-pane*
  [child arg]
  (u/create-entity (ScrollPane. (u/get-obj child :object) arg)))

(defmacro scroll-pane
  [child arg & options]
  `(let [entity# (scroll-pane* ~child ~arg)]
     (u/calls! ^ScrollPane (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro scroll-pane!
  [entity k & options]
  `(u/call! ^ScrollPane (u/get-obj ~entity :object) ~k ~@options))

(defn select-box*
  [items arg]
  (u/create-entity (SelectBox. (into-array items) arg)))

(defmacro select-box
  [items arg & options]
  `(let [entity# (select-box* ~items ~arg)]
     (u/calls! ^SelectBox (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro select-box!
  [entity k & options]
  `(u/call! ^SelectBox (u/get-obj ~entity :object) ~k ~@options))

(defn slider*
  [{:keys [min max step vertical?]
    :or {min 0 max 10 step 1 vertical? false}}
   arg]
  (u/create-entity
    (Slider. (float min) (float max) (float step) vertical? arg)))

(defmacro slider
  [attrs arg & options]
  `(let [entity# (slider* ~attrs ~arg)]
     (u/calls! ^Slider (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro slider!
  [entity k & options]
  `(u/call! ^Slider (u/get-obj ~entity :object) ~k ~@options))

(defn stack*
  [children]
  (create-group (Stack.) children))

(defmacro stack
  [children & options]
  `(let [entity# (stack* ~children)]
     (u/calls! ^Stack (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro stack!
  [entity k & options]
  `(u/call! ^Stack (u/get-obj ~entity :object) ~k ~@options))

(defn table*
  [children]
  (create-group (Table.) children))

(defmacro table
  [children & options]
  `(let [entity# (table* ~children)]
     (u/calls! ^Table (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro table!
  [entity k & options]
  `(u/call! ^Table (u/get-obj ~entity :object) ~k ~@options))

(defn text-button*
  [^String text arg]
  (u/create-entity (TextButton. text arg)))

(defmacro text-button
  [text arg & options]
  `(let [entity# (text-button* ~text ~arg)]
     (u/calls! ^TextButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro text-button!
  [entity k & options]
  `(u/call! ^TextButton (u/get-obj ~entity :object) ~k ~@options))

(defn text-field*
  [^String text arg]
  (u/create-entity (TextField. text arg)))

(defmacro text-field
  [text arg & options]
  `(let [entity# (text-field* ~text ~arg)]
     (u/calls! ^TextField (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro text-field!
  [entity k & options]
  `(u/call! ^TextField (u/get-obj ~entity :object) ~k ~@options))

(defn tree*
  [children arg]
  (create-group (Tree. arg) children))

(defmacro tree
  [children arg & options]
  `(let [entity# (tree* ~children ~arg)]
     (u/calls! ^Tree (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro tree!
  [entity k & options]
  `(u/call! ^Tree (u/get-obj ~entity :object) ~k ~@options))

(defn vertical*
  [children]
  (create-group (VerticalGroup.) children))

(defmacro vertical
  [children & options]
  `(let [entity# (vertical* ~children)]
     (u/calls! ^VerticalGroup (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro vertical!
  [entity k & options]
  `(u/call! ^VerticalGroup (u/get-obj ~entity :object) ~k ~@options))

(defn window*
  [children ^String title arg]
  (create-group (Window. title arg) children))

(defmacro window
  [children title arg & options]
  `(let [entity# (window* ~children ~title ~arg)]
     (u/calls! ^Window (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro window!
  [entity k & options]
  `(u/call! ^Window (u/get-obj ~entity :object) ~k ~@options))
