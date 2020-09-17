package com.ilsian.commonweb.res;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public class Loader {
	
	public static Configuration createTemplateLoader(Class appClass, String prefix) {
		
		// create a multi-source template loader to load the app's FTL files or the core FTL files
		final Configuration c = new Configuration();
		final MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[] { 
				new ClassTemplateLoader(appClass, prefix),
				new ClassTemplateLoader(Loader.class, "ftl")
				});

		c.setTemplateLoader(mtl);
		c.setObjectWrapper(new DefaultObjectWrapper());
		return c;
	}
	
	public static Configuration createTemplateLoader() {
		
		// create a single template loader to load the core FTL files
		final Configuration c = new Configuration();
		c.setTemplateLoader(new ClassTemplateLoader(Loader.class, "ftl"));
		c.setObjectWrapper(new DefaultObjectWrapper());
		return c;
	}
}
