package dev.queiroz.applemusic.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.queiroz.applemusic.R
import dev.queiroz.applemusic.databinding.FragmentHomeBinding
import dev.queiroz.applemusic.model.Song
import dev.queiroz.applemusic.ui.adapters.HighlightSongsRecyclerViewAdapter
import dev.queiroz.applemusic.ui.adapters.TopPicksRecyclerViewAdapter
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel

class HomeFragment : Fragment() {
    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val appleMusicViewModel: AppleMusicViewModel by activityViewModels()
    private val topPicksRecyclerViewAdapter = TopPicksRecyclerViewAdapter()
    private val madeForYouRecyclerViewAdapter = TopPicksRecyclerViewAdapter()
    private val recentPlayedRecyclerViewAdapter = HighlightSongsRecyclerViewAdapter()
    private val swedenArtistsRecyclerViewAdapter = HighlightSongsRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerViews()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    private fun setupRecyclerViews() {
        with(binding) {
            recyclerViewTopPicks.apply {
                topPicksRecyclerViewAdapter.onItemClickListener = {song ->
                    playMusicAndOpenPlayer(song)
                }
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = topPicksRecyclerViewAdapter
            }

            recyclerRecentPlayed.apply {
                recentPlayedRecyclerViewAdapter.onItemClickListener = {song ->
                    playMusicAndOpenPlayer(song)
                }
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = recentPlayedRecyclerViewAdapter
            }

            recyclerSwedenArtists.apply {
                swedenArtistsRecyclerViewAdapter.onItemClickListener = {song ->
                    playMusicAndOpenPlayer(song)
                }
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = swedenArtistsRecyclerViewAdapter
            }

            recyclerViewMadeForYou.apply {
                madeForYouRecyclerViewAdapter.onItemClickListener = {song ->
                    playMusicAndOpenPlayer(song)
                }
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = madeForYouRecyclerViewAdapter
            }
        }
    }

    private fun playMusicAndOpenPlayer(song: Song){
        appleMusicViewModel.playMusic(song)
        findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
    }

}