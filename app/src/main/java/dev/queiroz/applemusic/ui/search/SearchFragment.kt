package dev.queiroz.applemusic.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.queiroz.applemusic.databinding.FragmentSearchBinding
import dev.queiroz.applemusic.extensions.hideKeyboard
import dev.queiroz.applemusic.ui.adapters.SongListRecyclerAdapter
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

class SearchFragment : Fragment() {

    private val binding: FragmentSearchBinding by lazy {
        FragmentSearchBinding.inflate(layoutInflater)
    }
    private val appleMusicViewModel: AppleMusicViewModel by activityViewModels()

    private var searchTimer: TimerTask? = null

    private val songListAdapter = SongListRecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    private fun setupListeners() {
        binding.textInputSearchTerm.addTextChangedListener {
            searchTimer?.cancel()
            searchTimer = Timer().schedule(1500) {
                it.toString().let { term ->
                    appleMusicViewModel.findSongsByTerm(term)
                    hideKeyboard()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val decoration = DividerItemDecoration(
            context,
            LinearLayoutManager.VERTICAL
        )
        with(binding.recyclerSearchSongs) {
            songListAdapter.onItemClickListener = {song ->
                appleMusicViewModel.playMusic(song)
            }
            adapter = songListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(decoration)
        }
    }

    private fun setupObservers() {
        with(appleMusicViewModel){
            searchResultSongs.observe(this@SearchFragment) { songs ->
                songListAdapter.updateListOfSongs(songs)
            }

            loadingSearch.observe(this@SearchFragment){
                    binding.loadingListIndicator.visibility = if(it) View.VISIBLE else View.GONE
                    binding.recyclerSearchSongs.visibility = if(it) View.GONE else View.VISIBLE
            }
        }
    }
}