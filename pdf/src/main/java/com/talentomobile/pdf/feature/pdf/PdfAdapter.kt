package com.talentomobile.pdf.feature.pdf

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.talentomobile.pdf.databinding.PdfItemBinding
import com.talentomobile.pdf.feature.pdf.scaleimage.ImageSource

class PdfAdapter(private val core: PdfCore, private val isAnimation: Boolean) :
    RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val binding = PdfItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PdfViewHolder(binding, core)
    }

    override fun getItemCount(): Int = core.getPDFPagePage()
    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) = holder.bind(isAnimation)

    class PdfViewHolder(private val binding: PdfItemBinding, private val core: PdfCore) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(isAnimation: Boolean) = with(itemView) {
            binding.img.recycle()
            core.renderPage(adapterPosition) { bitmap: Bitmap?, currentPage: Int ->
                if (currentPage != adapterPosition) return@renderPage
                bitmap?.let(ImageSource::bitmap)?.let(binding.img::setImage)
                if (isAnimation) {
                    binding.img.animation = AlphaAnimation(0F, 1F).apply {
                        interpolator = LinearInterpolator()
                        duration = 500
                    }
                }
            }
        }
    }
}
