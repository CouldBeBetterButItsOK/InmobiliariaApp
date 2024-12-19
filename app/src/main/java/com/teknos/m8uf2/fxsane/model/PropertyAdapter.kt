import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.teknos.m8uf2.fxsane.R
import com.teknos.m8uf2.fxsane.databinding.ItemRecyclerviewBinding
import com.teknos.m8uf2.fxsane.fragment.RealEstateFragment
import com.teknos.m8uf2.fxsane.model.Propietat
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PropertyAdapter(
    private var properties: List<Propietat>,
    private val listener: (Propietat) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropertyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return properties.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val currentItem = properties[position]

        // Set property details
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        holder.binding.propertyName.text = currentItem.name
        holder.binding.propertyImage.setImageResource(R.drawable.property_pc)
        holder.binding.propertyTypeAndSize.text = "${currentItem.type} • ${currentItem.m2} m²"
        holder.binding.propertyPrice.text = "${currentItem.price}€"
        holder.binding.propertyLastUpdate.text = "Updated: ${currentItem.lastUpdate.toInstant().atZone(
            ZoneId.systemDefault()).toLocalDate().format(formatter)}"

        // Handle sold state
        if (currentItem.sold) {
            holder.binding.propertySoldLabel.visibility = View.VISIBLE
        } else {
            holder.binding.propertySoldLabel.visibility = View.GONE
        }

        // Set click listener
        holder.binding.root.setOnClickListener { listener(currentItem) }
    }

    fun updateList(newList: List<Propietat>) {
        properties = newList
        notifyDataSetChanged()
    }

    class PropertyViewHolder(val binding: ItemRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)
}
