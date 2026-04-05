class Movie {
        String titleVn, titleEn, duration, ageRating, rating,genre, description, director, actors, releaseDate, posterurl;

        public Movie(int idmovie, String titleVn, String titleEn, String duration, String ageRating, String rating, String genre, String description, String director, String actors, String releaseDate, String posterurl) {
            this.titleVn = titleVn;
            this.titleEn = titleEn;
            this.duration = duration;
            this.ageRating = ageRating;
            this.rating = rating;
            this.genre = genre;
            this.description = description;
            this.director = director;
            this.actors = actors;
            this.releaseDate = releaseDate;
            this.posterurl = posterurl;
        }
    }