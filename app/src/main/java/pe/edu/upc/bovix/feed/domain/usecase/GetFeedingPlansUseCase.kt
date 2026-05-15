package pe.edu.upc.bovix.feed.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan
import pe.edu.upc.bovix.feed.domain.repository.FeedingRepository
import javax.inject.Inject

class GetFeedingPlansUseCase @Inject constructor(
    private val repository: FeedingRepository
) {
    operator fun invoke(): Flow<Resource<List<FeedingPlan>>> = repository.getFeedingPlans()
}
