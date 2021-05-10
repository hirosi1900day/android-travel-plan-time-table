package jp.travelplantodo

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter


class TravelPlanRoomFragmentStatePagerAdapter(fm: FragmentActivity): FragmentStateAdapter(fm){

    var travelPlanId = ""

    var getTravelPlanIdfun: ((String) -> Unit)? = null

    private var fragmentCallback : FragmentCallBack? = null

    val fragments = listOf(HomeFragment(), MessageFragment(), GiftFragment(), MemberFragment())


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        getTravelPlanIdfun!!.invoke(travelPlanId)
        getTravelPlanIdfun = {fragmentCallback!!.getTravelPlanId(it)}

        return fragments[position]
    }




}