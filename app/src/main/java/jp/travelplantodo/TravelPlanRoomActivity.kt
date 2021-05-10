package jp.travelplantodo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat.apply
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_travel_plan_room.*


class TravelPlanRoomActivity : AppCompatActivity() {


    
    private val travelPlanRoomFragmentStatePagerAdapter by lazy { TravelPlanRoomFragmentStatePagerAdapter(
    this) }



    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_plan_room)


        viewPager2.apply {
            adapter = travelPlanRoomFragmentStatePagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL // スワイプの向き横（ORIENTATION_VERTICAL を指定すれば縦スワイプで実装可能です）
            offscreenPageLimit = travelPlanRoomFragmentStatePagerAdapter.itemCount // ViewPager2で保持する画面数
        }

        var travelPlanId = intent.getStringExtra(EXTRA_TRAVEL_PLAN_ID)
        if (travelPlanId != null) {
            travelPlanRoomFragmentStatePagerAdapter.travelPlanId = travelPlanId
        }


        // BottomNavigationの設定
        bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            viewPager2.currentItem = when (item.itemId) {
                R.id.navigation_home -> 0
                R.id.navigation_message -> 1
                R.id.navigation_member -> 2
                R.id.navigation_gift -> 3
                else -> 0
            }

            return@OnNavigationItemSelectedListener true
        })
    }
}