package com.blanco.somelai.ui.home.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blanco.somelai.R

class WineDetailFragment : Fragment() {


    // TODO este fragment no se utiliza de momento. Lo podemos usar para abrir los vinos del Feedfragment y darles alguna putuacion y comentarios.
    // TODO tenemos el layout echo para esta vista.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wine_detail, container, false)
    }

}