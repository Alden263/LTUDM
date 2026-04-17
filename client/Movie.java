package client;
class Movie {
        String titleVn, titleEn,  genre, description, director, actors, releaseDate, posterurl, banner;
        String cinemaName, cinemaAddress, provider;
        int duration, ageRating, idmovie;
        java.util.List<SessionGroup> sessionGroups;

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
            this.sessionGroups = new java.util.ArrayList<>();
        }
        public Movie() {
            this.sessionGroups = new java.util.ArrayList<>();
        }

        static class SessionGroup {
            String groupName;
            java.util.List<SessionTime> sessions;

            SessionGroup(String groupName) {
                this.groupName = groupName;
                this.sessions = new java.util.ArrayList<>();
            }
        }

        static class SessionTime {
            String startTime;
            String endTime;

            SessionTime(String startTime, String endTime) {
                this.startTime = startTime;
                this.endTime = endTime;
            }
        }
    }