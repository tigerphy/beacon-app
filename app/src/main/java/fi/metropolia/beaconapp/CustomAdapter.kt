package fi.metropolia.beaconapp

import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*

class CustomAdapter(private val scanResultList: ArrayList<ScanResult?>)
    : RecyclerView.Adapter<CustomAdapter.ViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return scanResultList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = scanResultList[position]
        val deviceName: String? = result?.device?.name
        val isConnectable: Boolean? = result?.isConnectable

        holder.textViewDeviceName.text = deviceName ?: "Unknown device"
        holder.textViewDeviceAddress.text = result?.device?.address
        holder.textViewSignalStrength.text = result?.rssi.toString()

        if (isConnectable == true) {
            val disabled = 0.4f
            holder.textViewDeviceName.alpha = disabled
            holder.textViewDeviceAddress.alpha = disabled
            holder.textViewSignalStrength. alpha = disabled
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textViewDeviceName = itemView.deviceNameTv as TextView
        val textViewDeviceAddress = itemView.deviceAddressTv as TextView
        val textViewSignalStrength = itemView.signalStrengthTv as TextView
    }

}