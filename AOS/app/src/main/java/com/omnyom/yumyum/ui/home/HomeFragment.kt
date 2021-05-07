package com.omnyom.yumyum.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.omnyom.yumyum.R
import com.omnyom.yumyum.databinding.FragmentHomeBinding
import com.omnyom.yumyum.databinding.ListItemFoodBinding
import com.omnyom.yumyum.helper.PreferencesManager
import com.omnyom.yumyum.helper.RetrofitManager.Companion.retrofitService
import com.omnyom.yumyum.model.feed.FeedData
import com.omnyom.yumyum.model.like.LikeRequest
import com.omnyom.yumyum.model.like.LikeResponse
import com.omnyom.yumyum.model.place.GetPlaceDataResponse
import com.omnyom.yumyum.model.place.PlaceData
import com.omnyom.yumyum.ui.search.SearchFragment
import com.omnyom.yumyum.ui.userfeed.UserFeedActivity
import com.omnyom.yumyum.ui.useroption.MyOptionActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        homeViewModel.foodData.observe(viewLifecycleOwner, Observer {
            binding.viewPagerHome.adapter = FeedPagesAdapter(context, it)
            Log.d("HomFrag", "${it}")
        })

        binding.viewPagerHome.orientation = ViewPager2.ORIENTATION_VERTICAL

        binding.icSearch.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, SearchFragment())
            transaction.commit()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 어탭터 형성
    class FeedPagesAdapter(val context: Context?,foodList: List<FeedData>) : RecyclerView.Adapter<FeedPagesAdapter.Holder>() {
        var item = foodList
        val userId = PreferencesManager.getLong(context!!, "userId").toString().toInt()


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : Holder {
            val innerBinding = ListItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)


            return Holder(innerBinding)
        }

        override fun getItemCount(): Int = item.size

        override fun onBindViewHolder(holder: Holder, position: Int) {

            // 좋아요!
            fun likeFeed() {
                var Call = retrofitService.feedLike(LikeRequest(item[position].id, userId).get())
                Call.enqueue(object : Callback<LikeResponse> {
                    override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                        if (response.isSuccessful) {
                        }
                    }

                    override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                        t
                    }

                })
            }

            // 안좋아요!
            fun unlikeFeed() {
                var Call = retrofitService.cancelFeedLike(item[position].id.toLong(), userId.toLong())
                Call.enqueue(object : Callback<LikeResponse> {
                    override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                        if (response.isSuccessful) {
                        }
                    }
                    override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                        t
                    }

                })
            }

            fun goUserFeed() {
                val intent = Intent(context, UserFeedActivity::class.java)
                val authorId = item[position].user.id.toString()

                intent.putExtra("authorId", authorId)
                Log.d("check1", "${intent.getStringExtra("authorId")}")
                context?.startActivity(intent)
            }


            if (holder.expendable.lineCount == 1) {
                holder.btnExpend.visibility = View.GONE
            }

            holder.placeName.text = item[position].place.name
            holder.address.text = item[position].place.address
            holder.food.setVideoURI(item[position].videoPath.toUri())
            holder.foodName.text = item[position].title
            holder.detail.text = item[position].content
            holder.userName.text = item[position].user.nickname
            holder.userName.setOnClickListener{
                goUserFeed()
            }

            holder.thumbUp.setMaxFrame(15)
            holder.thumbUp2.setMinFrame(15)

            // 버튼 구현
            if (item[position].isLike) {
                holder.thumbUp.visibility = View.INVISIBLE
            } else {
                holder.thumbUp2.visibility = View.INVISIBLE
            }

            holder.thumbUp.setOnClickListener {
                likeFeed()
                Log.d("nanta", "쪼아요")
                holder.thumbUp.playAnimation()
                Handler().postDelayed({
                    holder.thumbUp.progress = 0.0f
                    holder.thumbUp.visibility = View.INVISIBLE
                    holder.thumbUp2.visibility = View.VISIBLE
                }, 800)
            }

            holder.thumbUp2.setOnClickListener {
                unlikeFeed()
                Log.d("nanta", "씨러요")
                holder.thumbUp2.playAnimation()
                Handler().postDelayed({
                    holder.thumbUp2.progress = 0.5f
                    holder.thumbUp2.visibility = View.INVISIBLE
                    holder.thumbUp.visibility = View.VISIBLE
                }, 800)
            }


            // 루프 설정!
            holder.food.setOnPreparedListener { mp -> //Start Playback
                holder.food.start()
                //Loop Video
                mp.setVolume(0f,0f)
                mp!!.isLooping = true;
            };



        }

        class Holder(private val innerBinding: ListItemFoodBinding) : RecyclerView.ViewHolder(innerBinding.root) {
            val expendable = innerBinding.expandableText
            val btnExpend = innerBinding.expandCollapse
            val food = innerBinding.foodVideo
            val foodName = innerBinding.textName
            val placeName = innerBinding.textPlacename
            val address = innerBinding.textAddress
            val detail = innerBinding.textDetail
            val userName = innerBinding.textUser
            val thumbUp = innerBinding.avThumbUp
            val thumbUp2 = innerBinding.avThumbUp2
        }
    }
}