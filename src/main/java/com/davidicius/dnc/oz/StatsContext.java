package com.davidicius.dnc.oz;

import java.util.List;
import java.util.Set;

public class StatsContext {
    List<Domain> domains;
    List<StatOz> upvList;
    List<StatOz> ohimList;
    Set<StatOz> allOZ;
    Set<StatOz> filteredOZ;
    Set<String> czDictionary;
    Set<String> enCorpus;
    Set<String> czCorpus;
    Set<String> czCities;
    Set<String> czNames;
    Set<String> allCorpus;
    Set<String> keysFromOz;
    List<Domain> czDomains;
    StringIndex allCorpus2FilteredDomains;
    Set<String> ozWithHitInCzDomains;
    StringIndex filteredCorpus2DomainSorted;
    StringIndex oz2Domain;
    StringIndex czDomains2CorpusLine;
    OZComparator ozComparator;
    List<Domain> detailedDomains;

    public StatsContext(List<Domain> domains, List<StatOz> upvList, List<StatOz> ohimList, Set<StatOz> allOZ, Set<StatOz> filteredOZ, Set<String> czDictionary, Set<String> enCorpus, Set<String> czCorpus, Set<String> czCities, Set<String> czNames,
                        Set<String> allCorpus, Set<String> keysFromOz, List<Domain> czDomains, StringIndex allCorpus2FilteredDomains,
                        Set<String> ozWithHitInCzDomains, StringIndex filteredCorpus2DomainSorted, StringIndex oz2Domain, StringIndex czDomains2CorpusLine, List<Domain> detailedDomains) {
        this.domains = domains;
        this.upvList = upvList;
        this.ohimList = ohimList;
        this.allOZ = allOZ;
        this.filteredOZ = filteredOZ;
        this.czDictionary = czDictionary;
        this.enCorpus = enCorpus;
        this.czCorpus = czCorpus;
        this.czCities = czCities;
        this.czNames = czNames;
        this.allCorpus = allCorpus;
        this.keysFromOz = keysFromOz;
        this.czDomains = czDomains;
        this.allCorpus2FilteredDomains = allCorpus2FilteredDomains;
        this.ozWithHitInCzDomains = ozWithHitInCzDomains;
        this.filteredCorpus2DomainSorted = filteredCorpus2DomainSorted;
        this.oz2Domain = oz2Domain;
        this.czDomains2CorpusLine = czDomains2CorpusLine;
        this.ozComparator = new OZComparator(this);
        this.detailedDomains = detailedDomains;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public List<StatOz> getUpvList() {
        return upvList;
    }

    public List<StatOz> getOhimList() {
        return ohimList;
    }

    public Set<String> getCzDictionary() {
        return czDictionary;
    }

    public Set<String> getEnCorpus() {
        return enCorpus;
    }

    public Set<String> getCzCorpus() {
        return czCorpus;
    }

    public Set<String> getCzCities() {
        return czCities;
    }

    public Set<String> getCzNames() {
        return czNames;
    }

    public Set<StatOz> getAllOZ() {
        return allOZ;
    }

    public Set<StatOz> getFilteredOZ() {
        return filteredOZ;
    }

    public Set<String> getAllCorpus() {
        return allCorpus;
    }

    public Set<String> getKeysFromOz() {
        return keysFromOz;
    }

    public List<Domain> getCzDomains() {
        return czDomains;
    }

    public StringIndex getAllCorpus2FilteredDomains() {
        return allCorpus2FilteredDomains;
    }

    public Set<String> getOzWithHitInCzDomains() {
        return ozWithHitInCzDomains;
    }

    public StringIndex getFilteredCorpus2DomainSorted() {
        return filteredCorpus2DomainSorted;
    }

    public StringIndex getOz2Domain() {
        return oz2Domain;
    }

    public OZComparator getOzComparator() {
        return ozComparator;
    }

    public StringIndex getCzDomains2CorpusLine() {
        return czDomains2CorpusLine;
    }

    public List<Domain> getDetailedDomains() {
        return detailedDomains;
    }
}
