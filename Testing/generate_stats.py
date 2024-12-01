import pandas as pd
from statistics import multimode, mean, median, stdev
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

def calculate_statistics(execution_times):
    """Calculate mean, median, mode, and standard deviation."""
    mean_time = mean(execution_times)
    median_time = median(execution_times)
    modes = multimode(execution_times)
    std_dev = stdev(execution_times)
    
    # Handle multiple modes
    if len(modes) == 1:
        mode_time = modes[0]
    else:
        mode_time = modes  # List of modes
    
    return mean_time, median_time, mode_time, std_dev

def plot_histogram(execution_times, mean_time, median_time, mode_time, std_dev):
    """Plot a histogram of execution times with mean, median, mode, and standard deviation."""
    plt.figure(figsize=(10, 6))
    
    # Define bin range to focus between 25 and 35 ms
    bin_start = 25
    bin_end = 35
    bins = list(range(bin_start, bin_end + 1))  # Bins from 25 to 35
    
    # Plot the main histogram
    plt.hist(execution_times, bins=bins, edgecolor='black', alpha=0.7, label='Execution Times (25-35 ms)')
    
    # Plot outliers (below 25 or above 35 ms)
    outliers = [x for x in execution_times if x < bin_start or x > bin_end]
    if outliers:
        # Determine appropriate bins for outliers
        outlier_min = min(outliers)
        outlier_max = max(outliers)
        outlier_bins = list(range(outlier_min, outlier_max + 2))
        plt.hist(outliers, bins=outlier_bins, edgecolor='black', alpha=0.7, color='red', label='Outliers (<25 or >35 ms)')
    
    plt.title('Histogram of Execution Times')
    plt.xlabel('Execution Time (ms)')
    plt.ylabel('Frequency')
    
    # Plot mean
    plt.axvline(mean_time, color='blue', linestyle='dashed', linewidth=1.5, label=f'Mean: {mean_time:.2f} ms')
    
    # Plot median
    plt.axvline(median_time, color='green', linestyle='dashed', linewidth=1.5, label=f'Median: {median_time} ms')
    
    # Plot mode(s)
    if isinstance(mode_time, list):
        for m in mode_time:
            plt.axvline(m, color='purple', linestyle='dashed', linewidth=1.5, label=f'Mode: {m} ms')
    else:
        plt.axvline(mode_time, color='purple', linestyle='dashed', linewidth=1.5, label=f'Mode: {mode_time} ms')
    
    # Shade the area within one standard deviation from the mean
    plt.axvspan(mean_time - std_dev, mean_time + std_dev, color='yellow', alpha=0.2, label='±1 Standard Deviation')
    
    # Set x-axis limits to focus on 25-35 ms with some padding for outliers
    plt.xlim(bin_start - 5, bin_end + 5)  # Extending a bit to show outliers
    
    plt.legend()
    plt.tight_layout()
    return plt.gcf()  # Return the current figure

def plot_boxplot(execution_times):
    """Plot a box plot of execution times."""
    plt.figure(figsize=(10, 6))
    plt.boxplot(execution_times, vert=False, patch_artist=True, boxprops=dict(facecolor='lightblue'))
    plt.title('Box Plot of Execution Times')
    plt.xlabel('Execution Time (ms)')
    plt.tight_layout()
    return plt.gcf()

def generate_pdf_report(csv_file, output_pdf):
    """Generate a PDF report containing statistics and visualizations."""
    # Read the CSV file with error handling
    try:
        data = pd.read_csv(csv_file)
    except FileNotFoundError:
        print(f"Error: The file '{csv_file}' was not found.")
        return
    except pd.errors.EmptyDataError:
        print(f"Error: The file '{csv_file}' is empty.")
        return
    except pd.errors.ParserError:
        print(f"Error: The file '{csv_file}' does not appear to be in CSV format.")
        return
    
    # Check if 'ExecutionTime_ms' column exists
    if 'ExecutionTime_ms' not in data.columns:
        print("Error: 'ExecutionTime_ms' column not found in the CSV file.")
        return
    
    # Extract execution times
    execution_times = data['ExecutionTime_ms'].tolist()
    
    # Validate execution times
    if not execution_times:
        print("Error: No execution time data found.")
        return
    
    # Check for non-numeric values
    non_numeric = [x for x in execution_times if not isinstance(x, (int, float))]
    if non_numeric:
        print("Error: Non-numeric values found in 'ExecutionTime_ms' column.")
        print(non_numeric)
        return
    
    # Check if there are enough data points for standard deviation
    if len(execution_times) < 2:
        print("Error: At least two execution time data points are required to calculate standard deviation.")
        return
    
    # Calculate statistics
    mean_time, median_time, mode_time, std_dev = calculate_statistics(execution_times)
    
    # Debug print statements
    print(f"Mean: {mean_time:.2f} ms")
    print(f"Median: {median_time} ms")
    print(f"Mode: {mode_time if isinstance(mode_time, list) else [mode_time]} ms")
    print(f"Standard Deviation: {std_dev:.2f} ms")
    
    # Create histogram plot with standard deviation shaded
    fig_hist = plot_histogram(execution_times, mean_time, median_time, mode_time, std_dev)
    
    # Create box plot
    fig_box = plot_boxplot(execution_times)
    
    # Prepare statistics text
    if isinstance(mode_time, list):
        mode_str = ', '.join(map(str, mode_time))
    else:
        mode_str = str(mode_time)
    
    stats_text = f"""
    Execution Time Statistics
    =========================

    Total Runs: {len(execution_times)}
    
    Mean: {mean_time:.2f} ms
    Median: {median_time} ms
    Mode: {mode_str} ms
    Standard Deviation: {std_dev:.2f} ms
    """
    
    # Create PDF
    with PdfPages(output_pdf) as pdf:
        # Page 1: Histogram
        pdf.savefig(fig_hist)
        plt.close(fig_hist)
        
        # Page 2: Box Plot
        pdf.savefig(fig_box)
        plt.close(fig_box)
        
        # Page 3: Statistics Summary
        plt.figure(figsize=(8.5, 11))
        plt.axis('off')  # Hide axes
        
        # Add text to the figure
        plt.text(0.5, 0.5, stats_text, horizontalalignment='center', verticalalignment='center', fontsize=12, wrap=True)
        
        # Add the statistics page to the PDF
        pdf.savefig()
        plt.close()
    
    print(f"PDF report '{output_pdf}' has been generated successfully.")

if __name__ == "__main__":
    # Define input and output files
    csv_file = 'execution_times.csv'
    output_pdf = 'execution_time_report.pdf'
    
    # Generate the PDF report
    generate_pdf_report(csv_file, output_pdf)
