# SimpleBouncyRecyclerView
Super simple, basic, bouncy RecyclerView in Kotlin.

This is in no-way the best sollution to this problem/feature, it was more just something I messed around with and wanted to test out sharing aac libraries via bintray too!

![SimpleBouncyRecylerView](https://github.com/IainS1986/SimpleBouncyRecylerView/blob/master/docs/gifs/simple.gif)

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

Thats it! By default, you should see your RecyclerView "bouncing".

Note: Do **NOT** set the RecyclerView LayoutManager to something else, as that will remove the Bouncy support.


### Settings

There are a few settings you can apply both in the axml directly, and also in code via exposed properties in the SimpleBouncyRecyclerView.

**friction** - A 'faked' physics property that will alter how much you can 'pull' the recycler view, and how much it will overscroll on a fling. By default this is **1.0**, the higher the value the less you will be able to 'pull' the recycler view (2.0 would half the amount, 0.5 would double it)

**tension** - A 'faked' physics property that simple alters the speed at which the recyler view will animate back into place. The default is **1.0**. A higher value will result in the recyler view snapping back to its origin quicker. 2.0 would animate back twice as fast, 0.5 would animate back twice as slow.

**startIndexOffset** - This allows you to offset where the "bounce" happens at the start of the recyler view, for example after a Header Cell (see examples below). Default is **0** so the bounce is at the top of the recycler view.

**endIndexOffset** - This allows you to offset where the "bounce" happens at the end of the recyler view, for example after a Footer Cell (see examples below). Default is **0** so the bounce is at the bottom of the recycler view.

**startOverscrollColor** - This is a color resource to denote what the overscroll area at the start of the recyler view looks like. By default its **transparent** but you might have a usage where you want the color to match a Header Cell or something similar. See examples below.

**endOverscrollColor** - This is a color resource to denote what the overscroll area at the end of the recyler view looks like. By default its **transparent** but you might have a usage where you want the color to match a Footer Cell or something similar. See examples below.
