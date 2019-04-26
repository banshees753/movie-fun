package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {


    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager platformTransactionManagerAlbums;
    private final PlatformTransactionManager platformTransactionManagerMovies;


    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures, PlatformTransactionManager platformTransactionManagerAlbums, PlatformTransactionManager platformTransactionManagerMovies) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.platformTransactionManagerAlbums = platformTransactionManagerAlbums;
        this.platformTransactionManagerMovies = platformTransactionManagerMovies;
    }

    public void addAlbum(Album album) {
            albumsBean.addAlbum(album);

    }

    public void addMovie(Movie movie) {
                moviesBean.addMovie(movie);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {
        new TransactionTemplate(platformTransactionManagerMovies).execute(status -> {
            for (Movie movie : movieFixtures.load()) {
                addMovie(movie);
            }
            return null;
        });

        new TransactionTemplate(platformTransactionManagerAlbums).execute(status -> {
            for (Album album : albumFixtures.load()) {
                addAlbum(album);
            }
            return null;
        });

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
