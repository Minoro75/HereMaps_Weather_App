package io.minoro75.heremapsweatherapp.ui.city_weather

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import io.minoro75.heremapsweatherapp.R
import io.minoro75.heremapsweatherapp.databinding.FragmentCityWeatherBinding
import io.minoro75.heremapsweatherapp.utils.Status
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CityWeatherFragment : Fragment(R.layout.fragment_city_weather) {

    private val binding: FragmentCityWeatherBinding by viewBinding()
    private val args: CityWeatherFragmentArgs by navArgs()
    private val viewModel: CityWeatherViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvWeatherList.layoutManager = LinearLayoutManager(context)
        val weatherAdapter = WeatherAdapter(arrayListOf())
        binding.rvWeatherList.adapter = weatherAdapter

        binding.srlSwipeContainer.setOnRefreshListener {
            weatherAdapter.clear()
            viewModel.getWeatherInCity(args.city)
        }
        binding.srlSwipeContainer.setColorSchemeResources(R.color.primaryDarkColor)
        binding.btBack.setOnClickListener {
            findNavController().navigateUp()
        }
        lifecycleScope.launch {
            viewModel.getWeatherInCity(args.city)
            viewModel.weather.observe(viewLifecycleOwner, {
                when (it.status) {
                    is Status.SUCCESS -> {
                        binding.srlSwipeContainer.isRefreshing = false
                        weatherAdapter.clear()
                        weatherAdapter.addList(it.data)
                        binding.tvCurrentWeatherTemp.text = it.data?.first()?.temperature?.toInt().toString()
                        binding.tvCurrentDate.text = it.data?.first()?.utcTime
                        binding.tvCurrentWeatherDescription.text = it.data?.first()?.description
                        Glide.with(requireContext())
                            .load(it.data?.first()?.iconLink)
                            .centerCrop()
                            .into(binding.ivCurrentWeatherIcon)
                    }
                    is Status.ERROR -> {
                        Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            viewModel.city.observe(viewLifecycleOwner, {
                binding.tvCurrentLocation.text = it
            })
        }
    }
}
