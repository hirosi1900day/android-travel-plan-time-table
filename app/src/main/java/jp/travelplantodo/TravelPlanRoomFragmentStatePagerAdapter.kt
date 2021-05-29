package jp.travelplantodo

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import jp.travelplantodo.fragment.MessageFragment


class TravelPlanRoomFragmentStatePagerAdapter(fm: FragmentActivity): FragmentStateAdapter(fm){



    var travelPlanId = ""

    val fragments = listOf(HomeFragment(), MessageFragment(), MemberFragment(), GiftFragment(),SettingFragment())


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
//
        Log.d("確認postion","${position}")
         when (position) {
            0 -> {
                var fragment = HomeFragment()
                fragment.getPlanId(travelPlanId)
                return fragment
            }

            1 -> {
                var fragment = MessageFragment()
                fragment.getPlanId(travelPlanId)
                return fragment
            }

            2 -> {
                var fragment = MemberFragment()
                fragment.getPlanId(travelPlanId)
                return fragment
            }

            3 -> {
                var fragment = GiftFragment()
                fragment.getPlanId(travelPlanId)
                return fragment
            }

             4 -> {
                 var fragment = SettingFragment()
                 fragment.getPlanId(travelPlanId)
                 return fragment
             }

            else -> {
                var fragment = HomeFragment()
                fragment.getPlanId(travelPlanId)
                return fragment
            }
        }

    }
}