package com.stanford.simplebouncyrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stanford.simplebouncyrecyclerview.data.Movie

class MainActivity : AppCompatActivity() {

    private lateinit var _recyclerView : RecyclerView

    private val _cageMovies = listOf(
        Movie("Raising Arizona", 1987),
        Movie("Vampire's Kiss", 1988),
        Movie("Con Air", 1997),
        Movie("Face/Off", 1997),
        Movie("City of Angels", 1998),
        Movie("Snake Eyes", 1998),
        Movie("8mm", 1999),
        Movie("Gone in 60 Seconds", 2000),
        Movie("Matchstick Men", 2003),
        Movie("National Treasure", 2004),
        Movie("The Wicker Man", 2006),
        Movie("Ghost Rider", 2007),
        Movie("National Treasure: Book of Secrets", 2007),
        Movie("Knowing", 2009),
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

    override fun bind(movie: Movie) {
        _titleView?.text = movie.title
        _yearView?.text = movie.year.toString()
    }

}

class MovieHeaderViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    BaseViewHolder(R.layout.list_header, inflater, parent) {

    override fun bind(movie: Movie) {
    }

}

class MovieFooterViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    BaseViewHolder(R.layout.list_footer, inflater, parent) {

    override fun bind(movie: Movie) {
    }

}

abstract class BaseViewHolder(resource: Int, inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(resource, parent, false)) {

    abstract fun bind(movie: Movie)
}


class ListAdapter(private var list: List<Movie>) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var headerEnabled: Boolean = true
    var footerEnabled: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.list_header -> MovieHeaderViewHolder(inflater, parent)
            R.layout.list_footer -> MovieFooterViewHolder(inflater, parent)
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

        val movie: Movie = list[position - if (headerEnabled) 1 else 0]
        holder.bind(movie)
    }

}
