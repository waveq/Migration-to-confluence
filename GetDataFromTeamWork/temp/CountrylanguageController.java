package com.waveq.konkurs.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.waveq.konkurs.entity.Countrylanguage;
import com.waveq.konkurs.service.CountryDAO;
import com.waveq.konkurs.service.CountrylanguageDAO;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;

/**
 * 
 * @author Szymon
 */
@ManagedBean(name = "countrylanguageBean")
@SessionScoped
public class CountrylanguageController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CountrylanguageDAO clDAO;

	@Inject
	private CountryDAO cDAO;
	
	public List<Object[]> getAllCountryLanguages() {
		List<Object[]> list = clDAO.findAllCountrylanguages();
		double population = clDAO.getWholePopulation().get(0);
		for (Object[] row : list) {
			row[2] = (Double) row[2] / population * 10000;
			row[1] =  Math.floor((Double) row[1]);
		}
		return list;
	}

	public List<Object[]> getOfficialCountryLanguages() {
		List<Object[]> l = clDAO.findSortedOfficialCountrylanguages();
		for (Object[] row : l) {
			row[5] = (Integer) row[5] * (Float) row[4] * 0.01;
			row[5] =  Math.floor((Double) row[5]);
		}
		return l;
	}
	
	public String getCountryLanguagesJson(String code) {
		JsonArray array = new JsonArray();
		JsonObject lang = new JsonObject();
		
		List<Countrylanguage> list = clDAO.findCountryLanguagesByCode(code);
		
		int i = 0;
		for (Countrylanguage row : list) {
			i++;
			lang = new JsonObject();
			lang.addProperty("index", i);
			lang.addProperty("name", row.getCountrylanguagePK().getLanguage() 
					+ " - " + row.getPercentage() + "%");
			lang.addProperty("value", row.getPercentage());
			array.add(lang);
		}
		String json = new Gson().toJson(array);

		return json;
	}

	public String getCountryByCode(String code) {
		return cDAO.find(code).getName();
	}
}
