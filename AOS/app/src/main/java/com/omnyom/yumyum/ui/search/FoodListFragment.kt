package com.omnyom.yumyum.ui.search

import android.app.Activity
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.omnyom.yumyum.R
import com.omnyom.yumyum.databinding.FragmentFoodListBinding
import com.omnyom.yumyum.databinding.FragmentSearchInnerTabBinding
import com.omnyom.yumyum.helper.recycler.SearchFeedAdapter
import com.omnyom.yumyum.helper.recycler.SearchPlaceAdapter
import com.omnyom.yumyum.ui.base.BaseBindingFragment

class FoodListFragment : BaseBindingFragment<FragmentFoodListBinding>(R.layout.fragment_food_list) {
    private val searchVM: SearchViewModel by viewModels({requireParentFragment().requireParentFragment()})

    override fun extraSetupBinding() { }

    override fun setup() { }

    override fun setupViews() {
        binding.rvFood.apply {
            adapter = SearchFeedAdapter(activity as Activity).apply {
                setVM(searchVM)
            }
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onSubscribe() {
        searchVM.searchFeedResults.observe(viewLifecycleOwner, {
            val adapter = binding.rvFood.adapter as SearchFeedAdapter
            adapter.run {
                setItems(it)
                notifyDataSetChanged()
            }
        })
    }

    override fun release() { }
}