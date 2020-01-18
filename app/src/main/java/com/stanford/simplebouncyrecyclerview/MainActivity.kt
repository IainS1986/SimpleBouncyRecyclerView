package com.stanford.simplebouncyrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stanford.simplebouncyrecyclerview.data.Decade
import com.stanford.simplebouncyrecyclerview.data.ListItem
import com.stanford.simplebouncyrecyclerview.data.Movie

class MainActivity : AppCompatActivity() {

    private lateinit var _recyclerView : RecyclerView

    private val _cageMovies = listOf(
        Decade(1980),
        Movie("Raising Arizona", 1987),
        Movie("Vampire's Kiss", 1988),
        Decade(1990),
        Movie("Con Air", 1997),
        Movie("Face/Off", 1997),
        Movie("City of Angels", 1998),
        Movie("Snake Eyes", 1998),
        Movie("8mm", 1999),
        Decade(2000),
        Movie("Gone in 60 Seconds", 2000),
        Movie("Matchstick Men", 2003),
        Movie("National Treasure", 2004),
        Movie("The Wicker Man", 2006),
        Movie("Ghost Rider", 2007),
        Movie("National Treasure: Book of Secrets", 2007),
        Movie("Knowing", 2009),
        Decade(2010),
        Movie("Kick-Ass", 2010),
        Movie("Ghost Rider: Spirit of Vengeance", 2012)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _recyclerView = findViewById(R.id.list_view)

        _recyclerView.apply {
            adapter = ListAdapter(_cageMovies)
        }
    }
}

class MovieViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    BaseViewHolder(R.layout.list_item, inflater, parent) {

    private var _titleView: TextView? = null
    private var _yearView: TextView? = null

    init {
        _titleView = itemView.findViewById(R.id.list_title)
        _yearView = itemView.findViewById(R.id.list_description)
    }

    override fun bind(item: ListItem) {
        val movie = item as Movie
        _titleView?.text = movie.title
        _yearView?.text = movie.year.toString()
    }

}

class MovieSectionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    BaseViewHolder(R.layout.list_section, inflater, parent) {

    private var _titleView: TextView? = null

    init {
        _titleView = itemView.findViewById(R.id.list_section_text)
    }

    override fun bind(item: ListItem) {
        val section = item as Decade
        _titleView?.text = section.year.toString()
    }

}

class MovieHeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    BaseViewHolder(R.layout.list_header, inflater, parent) {

    override fun bind(item: ListItem) {
    }

}

class MovieFooterViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    BaseViewHolder(R.layout.list_footer, inflater, parent) {

    override fun bind(item: ListItem) {
    }

}

abstract class BaseViewHolder(resource: Int, inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(resource, parent, false)) {

    abstract fun bind(item: ListItem)
}


class ListAdapter(private var list: List<ListItem>) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var headerEnabled: Boolean = true
    var footerEnabled: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.list_header -> MovieHeaderViewHolder(inflater, parent)
            R.layout.list_footer -> MovieFooterViewHolder(inflater, parent)
            R.layout.list_section -> MovieSectionViewHolder(inflater, parent)
            else -> MovieViewHolder(inflater, parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (headerEnabled && position == 0)
        {
            return R.layout.list_header
        }

        if (footerEnabled && position == itemCount - 1)
        {
            return R.layout.list_footer
        }

        val item = list[position - if (headerEnabled) 1 else 0]

        if (item is Decade)
            return R.layout.list_section

        return R.layout.list_item
    }

    override fun getItemCount(): Int = list.size + (if (headerEnabled) 1 else 0) + (if (footerEnabled) 1 else 0)

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (headerEnabled && position == 0)
        {
            return
        }

        if (footerEnabled && position == itemCount - 1)
        {
            return
        }

        val item: ListItem = list[position - if (headerEnabled) 1 else 0]
        holder.bind(item)
    }

}
