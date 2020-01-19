# SimpleBouncyRecyclerView
Super simple, basic, bouncy RecyclerView in Kotlin.

This is in no-way the best sollution to this problem/feature, it was more just something I messed around with and wanted to test out sharing aac libraries via bintray too!

<img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/simple.gif" width="250" hspace="44">

This is a simple, "bouncy" RecyclerView with a few extra features to help support some basic use cases. Its not physics based, its just a simple overscroll handler that animates back in place when released (touch up).

Below are some example uses and the attributes you can use to achieve those results.


### Release

Current version is at **0.0.5**


### Install

To include SimpleBouncyRecyclerView in your project, add it to your **App build.gradle** dependency

```kotlin
dependencies {
    compile "com.stanford:simplebouncyrecycler:0.0.5"
}
```

You may also need to add jcenter to your repositories in your **project build.gradle**
```kotlin
repositories {
    ..
    jcenter()
        
}
```

### Usage

Once included in your apps gradle, usage is very simple. Just add a **com.stanford.simplebouncyrecycler.views.SimpleBouncyRecyclerView** to your layout axml.

```axml
<com.stanford.simplebouncyrecycler.views.SimpleBouncyRecyclerView
      android:id="@+id/list_view"
      android:layout_width="match_parent"
      android:layout_height="match_parent" />
```

Thats it! By default, you should see your RecyclerView "bouncing". This works for both Vertical and Horizontal RecyclerViews.

```diff
- Note: Do NOT set the RecyclerView LayoutManager to something else.
```

The full optional settings would look like...

```axml
<com.stanford.simplebouncyrecycler.views.SimpleBouncyRecyclerView
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/list_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:startIndexOffset="2"
        app:endIndexOffset="1"
        app:startOverscrollColor="#dddddd"
        app:endOverscrollColor="#000000FF"
        app:friction="1.0"
        app:tension="1.0" />
```

Similarly, you can access these values in code too...

```kotlin
var simpleBouncyRecyclerView: SimpleBouncyRecyclerView = findViewById(R.id.list_view)
simpleBouncyRecyclerView.startIndexOffset = 1
simpleBouncyRecyclerView.endIndexOffset = 1
simpleBouncyRecyclerView.tension = 0.75f
simpleBouncyRecyclerView.friction = 1.0f
simpleBouncyRecyclerView.endOverscrollColor = Color.RED
simpleBouncyRecyclerView.startOverscrollColor = resources.getColor(R.color.colorAccent)
```

### Settings

There are a few settings you can apply both in the axml directly, and also in code via exposed properties in the SimpleBouncyRecyclerView.

**friction** - A 'faked' physics property that will alter how much you can 'pull' the recycler view, and how much it will overscroll on a fling. By default this is **1.0**, the higher the value the less you will be able to 'pull' the recycler view (2.0 would half the amount, 0.5 would double it)

**tension** - A 'faked' physics property that simple alters the speed at which the recyler view will animate back into place. The default is **1.0**. A higher value will result in the recyler view snapping back to its origin quicker. 2.0 would animate back twice as fast, 0.5 would animate back twice as slow.

**startIndexOffset** - This allows you to offset where the "bounce" happens at the start of the recyler view, for example after a Header Cell (see examples below). Default is **0** so the bounce is at the top of the recycler view.

**endIndexOffset** - This allows you to offset where the "bounce" happens at the end of the recyler view, for example after a Footer Cell (see examples below). Default is **0** so the bounce is at the bottom of the recycler view.

**startOverscrollColor** - This is a color resource to denote what the overscroll area at the start of the recyler view looks like. By default its **transparent** but you might have a usage where you want the color to match a Header Cell or something similar. See examples below.

**endOverscrollColor** - This is a color resource to denote what the overscroll area at the end of the recyler view looks like. By default its **transparent** but you might have a usage where you want the color to match a Footer Cell or something similar. See examples below.

### Examples

This is the basic usage, no settings altered.

<img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/simple.gif" width="250" hspace="44">

Next, you can see the effect of **startIndexOffset**, a header cell has been added thats inside the RecyclerView itself, when theres no offset set, the header also bounces, by setting **startIndexOffset=1** in the axml, the header now remains static and is 'outside' the bounce.

<img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/header_nooffset.gif" width="250" hspace="44"> <img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/header_offset.gif" width="250" hspace="44">

Here you can see the same effect with **endIndexOffset**, a footer cell has been added and again you can see both before (**endIndexOffset=0**) shows the footer bouncing, and after (**endIndexOffset=1**) of the footer being outside the bounce.

<img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/footer_nooffset.gif" width="250" hspace="44"> <img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/footer_offset.gif" width="250" hspace="44">

The final examples show the usage of the **startOverscrollColor** (endOverscrollColor works in the same fashion, just at the end!). By default, the overscroll bounce region is **transparent**, but you may want to set a color for that region to produce different effects. New section cells have been added into the list (so now it goes, Header, Section, Items at the top).

The first gif shows the default behaviour when **startIndexOffset=1** for the header cell. The Section cell bounces too and theres white space above it. The second gif shows how if you set **startOverscrollColor** to be the same as the section color, then it that overscroll region makes it appear like the section cell expands.

<img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/sections_nooffset_nocolor.gif" width="250" hspace="44"> <img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/sections_nooffset_color.gif" width="250" hspace="44">

Similarly, you could set the offset to be **startIndexOffset=2** so the top section also remains clamped. Again the first gif shows the default overscroll color of **transaprent** and the second gif shows the **startOverscrollColor** being set to the same color as the section, giving a similar illusion of the section cell expanding above, but with the text appeared to be clamped differently!

<img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/sections_offset_nocolor.gif" width="250" hspace="44"> <img src="https://github.com/IainS1986/SimpleBouncyRecyclerView/blob/master/docs/gifs/sections_offset_color.gif" width="250" hspace="44">

Really, you'll possibly find no use of the **startOverscrollColor** or **endOverscrollColor** but they are there for if you need them.

