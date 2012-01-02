package hudson.plugins.label;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Result;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This {@link ListViewColumn} adds a (optional) new column to user defined
 * views that shows the label of the last build of a job.
 * 
 * @author nmahle
 */
public class LabelColumn extends ListViewColumn {

	private final String jobName4Label;
	private String result;

	@DataBoundConstructor
	public LabelColumn(String jobName4Label) {
		this.jobName4Label = jobName4Label;
	}
	
	 public LabelColumn() {
		 
		    this(null);
    }

	public String getJobName4Label() {
		return jobName4Label;
	}

	/** Method to get the label out of textfield and return it for the column
	 * @param Job job
	 * @return String result is the label which will shown in the column
	 * */
	public String getLabel(Job job) {
		//split textfield into jobs
		String[] jobNames = splitJobs(jobName4Label);
		// iterate over jobs in textfield
		for (String element : jobNames) {
			String[] jobName = null;
			String prefix = null;
			//job specific label prefix 
			String splitterBegin = "<";
			String splitterEnd = ">";
			if (element.contains(splitterBegin)) {
				jobName = element.split(splitterBegin);
				element = jobName[0];
				prefix = jobName[1];
			}
			// job has to be in textfield
			if (job.getName().equals(element)) {
				// special prefix -> use it
				if (prefix != null) {
					String[] tmpArray = prefix.split(splitterEnd);					
					String lastLabel = specialLabel(job, tmpArray[0]);
					result = lastLabel;
				} else {
					//no special prefix -> defaultLabel
					String lastLabel = defaultLabel(job);
					result = lastLabel;
				}
				prefix = null;
				break;
			} else {
				result = "kein Label vorhanden";
			}
		}
		return result;

	}

	/** Method to split textfield ";" separated JobNames   
	 * @param String jobName4Label textfield list of JobNames in one String
	 * @return String Array jobNames4Label contains JobNames
	 * */
	public String[] splitJobs(String jobName4Label) {
		//System.out.println(jobName4Label);
		String[] jobNames4Label = jobName4Label.split(";");
		return jobNames4Label;
	}

	/** default Method to set the label, which will shown in labelView - column
	 *  i.e. if jobName is DAILY and lastBuild is number 13 then lastLabel = DAILY-13    
	 * @param Job job 
	 * @return String lastLabel 
	 * */
	public String defaultLabel(Job job) {
		String build = "" + getLastCompletedandSuccessfullBuild(job);
		String labelnumber = build.split("#")[1];
		String lastLabel = job.getName() + "-" + labelnumber;
		return lastLabel;
	}

	/** special Method to set the label, which will shown in labelView - column
	 *  i.e. if prefix is "01.02.39-" and lastBuild is number 13 
	 *  then lastLabel = 01.02.39-13     
	 * @param Job job 
	 * @param String prefix which is the prefix label
	 * @return String lastLabel 
	 * */
	public String specialLabel(Job job, String prefix) {
		String build = getLastCompletedandSuccessfullBuild(job);
		String labelnumber = build.split("#")[1];
		String lastLabel = prefix + labelnumber;
		return lastLabel;
	}

	/*
	 * due to some strange, weird code behavior in job.getLastSuccessfulBuild in line 727
	 * we have to use an own method
	 */
	public String getLastCompletedandSuccessfullBuild(Job job){
		String lastCompletedandSuccessfullBuild=null;
		if (job.getLastCompletedBuild().getResult().equals(Result.SUCCESS)){
			lastCompletedandSuccessfullBuild = "" + job.getLastCompletedBuild();
			//System.out.println("job.getLastCompletedBuild(): "+job.getLastCompletedBuild());
			}
		else {
			lastCompletedandSuccessfullBuild = "" + job.getLastCompletedBuild().getPreviousSuccessfulBuild();
			//System.out.println("job.getLastCompletedBuild().getPreviousSuccessfulBuild(): "+job.getLastCompletedBuild().getPreviousSuccessfulBuild());
		}
		return lastCompletedandSuccessfullBuild;
	}
	
	@Extension
	public static class LabelColumnDescriptor extends ListViewColumnDescriptor {

		// DisplayName in configure view column
		@Override
		public String getDisplayName() {
			
			return "Label";
		}
		  // don't show the Label column in the ALL View by default
		  @Override
		  public boolean shownByDefault() {
		        return false;
		    }
	}
}