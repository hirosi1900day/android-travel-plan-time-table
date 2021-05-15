package jp.travelplantodo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.activity_travel_plan_room.*


class TravelPlanRoomFragmentStatePagerAdapter(fm: FragmentActivity): FragmentStateAdapter(fm){



    var travelPlanId = ""

    val fragments = listOf(HomeFragment(), MessageFragment(), MemberFragment(), GiftFragment())


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
//           HomeFragment().getPlanId(travelPlanId)
//           MessageFragment().getPlanId(travelPlanId)
//           MemberFragment().getPlanId(travelPlanId)

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
            else -> {
                var fragment = HomeFragment()
                fragment.getPlanId(travelPlanId)
                return fragment
            }
        }

    }
}