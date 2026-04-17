package client;

public class Movie {
    public String titleVn, titleEn, genre, description, director, actors, releaseDate, posterurl, banner;
    public String imdbRating, rottenRating;
    public int duration, ageRating, idmovie;
    
    // THÊM 3 BIẾN NÀY DÀNH CHO AI VÀ KHEN PHIM
    public String aiSummary, khenPhimTitle, khenPhimUrl;

    // CHÚ Ý THỨ TỰ THAM SỐ Ở ĐÂY (Giữ nguyên, không cần sửa)
    public Movie(int idmovie, String titleVn, String titleEn, int duration, int ageRating, 
                 String genre, String description, String director, String actors, 
                 String releaseDate, String imdbRating, String rottenRating, 
                 String posterurl, String banner) {
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
        this.imdbRating = imdbRating;      
        this.rottenRating = rottenRating;   
        this.posterurl = posterurl;         
        this.banner = banner;              
    }

    public Movie() {}
}