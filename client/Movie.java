package client;
class Movie {
        String titleVn, titleEn,  genre, description, director, actors, releaseDate, posterurl, banner;
        int duration, ageRating, idmovie;

        public Movie(int idmovie, String titleVn, String titleEn, int duration, int ageRating, String genre, String description, String director, String actors, String releaseDate, String posterurl, String banner) {
            this.idmovie = idmovie;
            this.titleVn = titleVn;
            this.titleEn = titleEn;
            this.duration = duration;
            this.ageRating = ageRating;
            this.genre = genre;
            this.description = description;
            this.director = director;
            this.actors = actors;
            this.releaseDate = releaseDate;
            this.posterurl = posterurl;
            this.banner = banner;
        }
        public Movie() {
        }
    }