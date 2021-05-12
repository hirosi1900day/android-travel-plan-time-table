package jp.travelplantodo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class TravelPlanRoomFragmentStatePagerAdapter(fm: FragmentActivity): FragmentStateAdapter(fm){



    var travelPlanId = ""




    val fragments = listOf(HomeFragment(), MessageFragment(), GiftFragment(), MemberFragment())


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {

        when(position){
            0 -> HomeFragment().getPlanId(travelPlanId)
            1 -> MessageFragment().getPlanId(travelPlanId)
            2 -> true
            3 -> MemberFragment().getPlanId(travelPlanId)
        }



        return fragments[position]
    }




}