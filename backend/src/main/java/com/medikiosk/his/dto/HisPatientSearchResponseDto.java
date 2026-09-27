package com.medikiosk.his.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response containing matched hospital patient candidates from HIS lookup.
 */
public class HisPatientSearchResponseDto {

    private List<HisPatientCandidateDto> candidates;
    private int totalFound;
    private String query;
    private String searchType;

    @JsonProperty("isMock")
    private boolean isMock;

    private String message;

    public HisPatientSearchResponseDto() {
    }

    public HisPatientSearchResponseDto(List<HisPatientCandidateDto> candidates, int totalFound, String query,
                                       String searchType, boolean isMock, String message) {
        this.candidates = candidates;
        this.totalFound = totalFound;
        this.query = query;
        this.searchType = searchType;
        this.isMock = isMock;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<HisPatientCandidateDto> candidates;
        private int totalFound;
        private String query;
        private String searchType;
        private boolean isMock;
        private String message;

        public Builder candidates(List<HisPatientCandidateDto> candidates) {
            this.candidates = candidates;
            return this;
        }

        public Builder totalFound(int totalFound) {
            this.totalFound = totalFound;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder searchType(String searchType) {
            this.searchType = searchType;
            return this;
        }

        public Builder isMock(boolean isMock) {
            this.isMock = isMock;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public HisPatientSearchResponseDto build() {
            return new HisPatientSearchResponseDto(candidates, totalFound, query, searchType, isMock, message);
        }
    }

    public List<HisPatientCandidateDto> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<HisPatientCandidateDto> candidates) {
        this.candidates = candidates;
    }

    public int getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(int totalFound) {
        this.totalFound = totalFound;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
