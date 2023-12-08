package com.example.fyp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fyp.Contract
import com.example.fyp.R

class ContractCardAdapter(private val productList: List<ContractCard>, private val clickListener: OnContractClickListener) : RecyclerView.Adapter<ContractCardAdapter.ProductViewHolder>(){

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val Name: TextView = view.findViewById(R.id.contractName)
        val Description: TextView = view.findViewById(R.id.contractDescription)
        val Image: ImageView = view.findViewById(R.id.contractImage)

        val send : Button = view.findViewById(R.id.send)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contract_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val contract = productList[position]
        holder.Name.text = contract.name
        holder.Description.text = contract.description
        Glide.with(holder.Image.context).load(contract.imageUrl).into(holder.Image)

        holder.itemView.setOnClickListener {
            clickListener.onContractClick(contract)
        }



        holder.send.setOnClickListener {
            clickListener.onSendButtonClick(contract, position)
        }
    }

    override fun getItemCount() = productList.size

    interface OnContractClickListener {
        fun onContractClick(contractCard: ContractCard)

        fun onSendButtonClick(contractCard: ContractCard, position: Int)
    }
}

data class ContractCard(
    val name: String,
    val description: String,
    val imageUrl: Int,
    val id : String
)