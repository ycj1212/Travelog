package com.thequietz.travelog.place.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thequietz.travelog.place.model.PlaceDetailGeometry
import com.thequietz.travelog.place.model.PlaceDetailImage
import com.thequietz.travelog.place.model.PlaceDetailLocation
import com.thequietz.travelog.place.model.PlaceDetailModel
import com.thequietz.travelog.place.model.PlaceDetailOperation
import com.thequietz.travelog.place.repository.PlaceDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val repository: PlaceDetailRepository
) : ViewModel() {
    private val TAG = "VIEWMODEL"

    private val typeMap = mapOf(
        12 to "관광지",
        14 to "문화시설",
        15 to "행사 및 축제",
        25 to "여행",
        28 to "레포츠",
        32 to "숙박시설",
        38 to "쇼핑",
        39 to "음식점",
    )

    private var _detail = MutableLiveData<PlaceDetailModel>()
    val detail: LiveData<PlaceDetailModel> get() = _detail

    private var _operation = MutableLiveData<String>()
    val operation: LiveData<String> get() = _operation

    var isLoaded = MutableLiveData(false)

    fun loadDetailBySearch(placeId: String) {
        viewModelScope.launch {
            val data = repository.loadPlaceDetail(placeId)
            Log.d(TAG, data.toString())

            data.let {
                _detail.value = it
            }
            _operation.value = data?.operation?.operation?.fold("", { acc, value ->
                val newValue = acc + value + "\n"
                newValue
            })
        }
    }

    fun loadDetailByRecommend(id: Long, typeId: Int) {
        Log.d(TAG, "Detail Data Loaded")
        viewModelScope.launch {
            val images = repository.loadPlaceImages(typeId, id)
            val info = repository.loadPlaceInfo(typeId, id)
            _detail.value = PlaceDetailModel(
                placeId = id.toString(),
                address = info?.address ?: "",
                phoneNumber = info?.phoneNumber ?: "",
                name = info?.name ?: "",
                operation = PlaceDetailOperation(true, listOf()),
                geometry = PlaceDetailGeometry(
                    PlaceDetailLocation(
                        info?.latitude ?: Double.NaN,
                        info?.longitude ?: Double.NaN
                    )
                ),
                images = images?.map { PlaceDetailImage(it.imageId, it.imageUrl) } ?: listOf(),
                reviews = listOf(),
                types = listOf(typeMap[typeId] ?: "Unknown"),
                website = info?.url ?: "",
                overview = info?.overview ?: ""
            )
        }
    }
}