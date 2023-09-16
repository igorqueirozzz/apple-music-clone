package dev.queiroz.applemusic.ui.search

import android.os.Bundle
import android.os.Handler

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.queiroz.applemusic.databinding.FragmentSearchBinding
import dev.queiroz.applemusic.exoplayer.MusicService
import dev.queiroz.applemusic.ui.adapters.SongListRecyclerAdapter
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel
import kotlinx.coroutines.MainScope
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
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
        setupSearchInputListener()
        setupObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    private fun setupSearchInputListener() {
        binding.textInputSearchTerm.addTextChangedListener {
            searchTimer?.cancel()
            searchTimer = Timer().schedule(3000) {
                it.toString().let { term ->
                    appleMusicViewModel.findSongsByTerm(term)
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
            adapter = songListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(decoration)
        }
    }

    private fun setupObservers() {
        appleMusicViewModel.searchResultSongs.observe(this) { songs ->
            songListAdapter.updateListOfSongs(songs)
        }
    }
}