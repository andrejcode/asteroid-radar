package com.andrejmilanovic.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.andrejmilanovic.asteroidradar.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {
    /**
     * Lazily initialized [MainViewModel]
     */
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the MainViewModel
        binding.viewModel = viewModel

        /* Sets the adapter of RecyclerView with clickHandler lambda that tells
        the viewModel when asteroid is clicked */
        binding.asteroidRecycler.adapter = MainAdapter(MainAdapter.OnClickListener {
            viewModel.displayAsteroidDetails(it)
        })

        // Observe navigateToSelectedAsteroid LiveData and navigate when it isn't null
        viewModel.navigateToSelectedAsteroid.observe(viewLifecycleOwner) {
            if (null != it) {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.displayAsteroidDetailsComplete()
            }
        }

        // Observe status LiveData and show loading ProgressBar accordingly
        viewModel.status.observe(viewLifecycleOwner) {
            when (it!!) {
                AsteroidApiStatus.LOADING -> binding.statusLoadingWheel.visibility = View.VISIBLE
                AsteroidApiStatus.ERROR -> binding.statusLoadingWheel.visibility = View.GONE
                AsteroidApiStatus.DONE -> binding.statusLoadingWheel.visibility = View.GONE
            }
        }

        /**
         * Show Snackbar whenever [viewModel.snackbar] is updated a non-null value
         */
        viewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                // Show error message
                Snackbar.make(requireView(), "Error: $text", Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        }
        return binding.root
    }
}