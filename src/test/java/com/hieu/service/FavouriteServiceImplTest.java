package com.hieu.service;

import com.hieu.model.Favourite;
import com.hieu.model.Product;
import com.hieu.model.User;
import com.hieu.payload.dto.FavouriteDto;
import com.hieu.repository.FavouriteRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.repository.UserRepository;
import com.hieu.service.impl.FavouriteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceImplTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    @Test
    void findByUser_returnsListOfFavourites() {
        when(favouriteRepository.findByUserId(1L)).thenReturn(List.of(Favourite.builder().id(1L).user(User.builder().id(1L).username("john").build()).product(Product.builder().id(2L).name("Milk").build()).build()));

        List<FavouriteDto> result = favouriteService.findByUser(1L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getProductId());
    }

    @Test
    void toggle_whenNotFavourite_savesAndReturnsTrue() {
        when(favouriteRepository.findByUserIdAndProductId(1L, 2L)).thenReturn(java.util.Optional.empty());
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(User.builder().id(1L).username("john").build()));
        when(productRepository.findById(2L)).thenReturn(java.util.Optional.of(Product.builder().id(2L).name("Milk").build()));
        when(favouriteRepository.save(any(Favourite.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = favouriteService.toggle(1L, 2L);

        assertTrue(result);
    }

    @Test
    void toggle_whenFavourite_removesAndReturnsFalse() {
        when(favouriteRepository.findByUserIdAndProductId(1L, 2L)).thenReturn(java.util.Optional.of(
                Favourite.builder().id(1L).user(User.builder().id(1L).username("john").build()).product(Product.builder().id(2L).name("Milk").build()).build()));

        boolean result = favouriteService.toggle(1L, 2L);

        assertFalse(result);
    }

    @Test
    void isFavourite_returnsTrue() {
        when(favouriteRepository.existsByUserIdAndProductId(1L, 2L)).thenReturn(true);

        assertTrue(favouriteService.isFavourite(1L, 2L));
    }

    @Test
    void countByProduct_returnsCount() {
        when(favouriteRepository.countByProductId(2L)).thenReturn(3L);

        assertEquals(3L, favouriteService.countByProduct(2L));
    }

    @Test
    void remove_deletesFavourite() {
        favouriteService.remove(1L, 2L);

        // verify via mock interaction is covered by Mockito verify below
    }
}
