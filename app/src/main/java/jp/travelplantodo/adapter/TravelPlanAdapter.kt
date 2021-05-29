package jp.travelplantodo.adapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import jp.travelplantodo.R
import jp.travelplantodo.MainActivity
import jp.travelplantodo.model.TravelPlan
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.list_taravelplan.view.*



class TravelPlanAdapter(context: MainActivity): BaseAdapter(){

    private var mLayoutInflater: LayoutInflater
    private var mTravelPlanArrayList = ArrayList<TravelPlan>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mTravelPlanArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mTravelPlanArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_taravelplan, parent, false)
        }

        val titleText = convertView!!.titleTextView as TextView
        titleText.text = mTravelPlanArrayList[position].title

        val nameText = convertView.bodyTextView as TextView
        nameText.text = mTravelPlanArrayList[position].body

        val travelListImageView = convertView.travelListImageView as ImageView
        if (mTravelPlanArrayList[position].image != "") {
            val imageString = mTravelPlanArrayList[position].image
            val bytes = Base64.decode(imageString, Base64.DEFAULT)
            val image =
                BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
            travelListImageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setTravelPlanArrayList(questionArrayList: ArrayList<TravelPlan>) {
        mTravelPlanArrayList = questionArrayList
    }

}