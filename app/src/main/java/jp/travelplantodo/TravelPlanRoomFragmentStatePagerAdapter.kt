package jp.travelplantodo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class TravelPlanRoomFragmentStatePagerAdapter(fm: FragmentActivity): FragmentStateAdapter(fm) {

    val fragments = listOf(HomeFragment(), MessageFragment(), GiftFragment(), MemberFragment())


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}