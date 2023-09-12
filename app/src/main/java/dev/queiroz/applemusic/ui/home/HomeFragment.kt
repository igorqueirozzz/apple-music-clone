package dev.queiroz.applemusic.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dev.queiroz.applemusic.databinding.FragmentHomeBinding
import dev.queiroz.applemusic.ui.adapters.HighlightSongsRecyclerViewAdapter
import dev.queiroz.applemusic.ui.adapters.TopPicksRecyclerViewAdapter
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel

class HomeFragment : Fragment() {
    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val appleMusicViewModel: AppleMusicViewModel by activityViewModels()

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
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = TopPicksRecyclerViewAdapter()
            }

            recyclerRecentPlayed.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = HighlightSongsRecyclerViewAdapter()
            }

            recyclerSwedenArtists.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = HighlightSongsRecyclerViewAdapter()
            }

            recyclerViewMadeForYou.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = TopPicksRecyclerViewAdapter()
            }
        }
    }

}